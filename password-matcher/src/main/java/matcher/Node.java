package matcher;

import codec.ClusterInitializer;
import codec.ITimerCallback;
import codec.MessageHandler;
import codec.NodeTimer;
import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.ClusterMessageHandler;
import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.net.Address;
import message.*;
import utils.ClusterStatus;
import utils.ConfigReader;
import utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ashan on 2020-05-03
 */
public class Node implements ClusterMessageHandler, ITimerCallback {
    private static long nodeID;
    private Cluster seedNode;
    private NodeType nodeType = NodeType.SLAVE;
    private List<AutoCloseable> resourceManager = new ArrayList<>();
    private int hbInterval = ConfigReader.getEnvironmentConfigsAsInteger("NODE_BH_INTERVAL", "5");
    private int leaderElectInterval = 6; //ConfigReader.getEnvironmentConfigsAsInteger("NODE_LEADER_ELECT_INTERVAL", "5");
    private int maxHBExpiryCount = ConfigReader.getEnvironmentConfigsAsInteger("NODE_MAX_HB_EXPIRE_COUNT", "3");
    private String memberID;
    private NodeTimer hbTimer;
    private NodeTimer leaderElectTimer;

    private AtomicLong hbMiscount = new AtomicLong(0);
    private AtomicReference<ClusterStatus> clusterStatus = new AtomicReference<>(ClusterStatus.WAIT_FOR_LEADER);
    private Address rootClusterAddress = Address.create(ConfigReader.getEnvironmentConfigs("SCALECUBE_CON_STR"),
            ConfigReader.getEnvironmentConfigsAsInteger("SCALECUBE_PORT", "7788"));
    private MessageHandler messageHandler;
    private AtomicBoolean electionResponseReceived = new AtomicBoolean(false);

    public Cluster getSeedNode() {
        return seedNode;
    }

    Node() {
        initialize();
    }

    private void initialize() {
        seedNode = new ClusterImpl().config(clusterConfig -> clusterConfig.memberAlias("PWM_" + ClusterInitializer.getCurrentNodeID()))
                .config(opts -> opts.metadata(Collections.singletonMap("nodeID", nodeID)))
                .membership(membershipConfig -> membershipConfig.seedMembers(rootClusterAddress))
                .handler(cluster -> this)
                .startAwait();
        memberID = seedNode.member().alias();
        System.out.println("MemberIDs :: " + memberID);
        nodeID = Long.parseLong(memberID.split("_")[1]);
        messageHandler = new MessageHandler(seedNode, nodeID);
        hbTimer = new NodeTimer(seedNode, "HB-" + memberID, true, hbInterval, false, this).start();
        resourceManager.add(hbTimer);
    }

    @Override
    public void onGossip(Message gossip) {
        Object msg = MessageParser.parse(gossip);
        if (msg instanceof HBMsg) {
            hbMiscount.set(0);
            System.out.println(memberID + "Leader HB received");
        } else if (msg instanceof LeaderElectionResultMsg) {
            System.out.println(memberID + "Leader Response Received");
        }
    }

    @Override
    public void onMembershipEvent(MembershipEvent event) {
    }

    @Override
    public void onMessage(Message message) {
        Object msg = MessageParser.parse(message);
        if (msg instanceof LeaderElectionRequestMsg) {
            System.out.println("LeaderElectionRequest received :: " + ((LeaderElectionRequestMsg) msg).getMemberID() + " :: " + memberID);
            clusterStatus.set(ClusterStatus.PENDING_ELECTION);
            messageHandler.sendReply(message.sender(), new LeaderElectionResponseMsg(memberID));
            startLeaderElection();
        } else if (msg instanceof LeaderElectionResponseMsg) {
            onLeaderElectionResponse((LeaderElectionResponseMsg) msg);
        } else if (msg instanceof TimerResponse) {
            this.onTimer(((TimerResponse) msg).getTimerID());
        } else if (msg instanceof HBMsg) {
            hbMiscount.set(0);
            System.out.println(memberID + " Slave HB received");
        } else if (msg instanceof LeaderElectionResultMsg) {
            System.out.println(memberID + " Leader Response Received");
        } else {
            System.out.println(memberID + " other Msg " + msg.toString());
        }
    }

    @Override
    public void onTimer(String timer) {
        if (timer.equalsIgnoreCase("HB-" + memberID)) {
            if (nodeType == NodeType.SLAVE) {
                if (hbMiscount.getAndIncrement() > maxHBExpiryCount && clusterStatus.get() == ClusterStatus.LEADER_ELECTED) {
                    startLeaderElection();
                }
            } else {
                messageHandler.sendReplyToAll(new HBMsg(TimeUtils.currentTimeMillis(), memberID));
            }
        } else if (timer.equalsIgnoreCase("LE-" + memberID) && !electionResponseReceived.get()) {
            System.out.println("Elected Leader :: " + memberID);
            this.updateNodeState();
        }
    }

    public void terminate() throws Exception {
        if (seedNode != null)
            seedNode.shutdown();

        if (leaderElectTimer != null)
            leaderElectTimer.shutdown();

        resourceManager.forEach(x -> {
            try {
                x.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startLeaderElection() {
        clusterStatus.set(ClusterStatus.PENDING_ELECTION);
        electionResponseReceived.set(false);
        messageHandler.sendLeaderElectionRequest();
        if (leaderElectTimer == null)
            leaderElectTimer = new NodeTimer(seedNode, "LE-" + memberID, false, leaderElectInterval, this).start();

    }

    private void onLeaderElectionResponse(LeaderElectionResponseMsg responseMsg) {
        System.out.println(TimeUtils.getCurrentTime() + " Leader election response received : " + responseMsg.getMemberID() + " : current no : " + memberID);
        electionResponseReceived.set(true);
        if (leaderElectTimer != null) {
            leaderElectTimer.shutdown();
            leaderElectTimer = null;
        }

    }

    private void updateNodeState() {
        nodeType = NodeType.MASTER;
        messageHandler.sendReplyToAll(new LeaderElectionResultMsg(seedNode.address()));
    }
}

