package matcher;

import codec.ITimerCallback;
import codec.MessageHandler;
import codec.NodeTimer;
import common.PasswordGenerator;
import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.ClusterMessageHandler;
import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.net.Address;
import message.*;
import message.LeaderElectionResultMsg.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ClusterStatus;
import utils.ConfigReader;
import utils.FileReader;
import utils.TimeUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static utils.ExceptionHandler.unhandled;

/**
 * @author ashan on 2020-05-03
 */
public class Node implements ClusterMessageHandler, ITimerCallback {
    private static long nodeID;
    private Cluster seedNode;
    private NodeType nodeType = NodeType.SLAVE;
    private List<AutoCloseable> resourceManager = new ArrayList<>();
    private int hbInterval = ConfigReader.getEnvironmentConfigsAsInteger("NODE_BH_INTERVAL", "5");
    private int leaderElectInterval = ConfigReader.getEnvironmentConfigsAsInteger("NODE_LEADER_ELECT_INTERVAL", "5");
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

    private Address masterNodeAddress;
    private List<String> passwordList;
    private PasswordGenerator passwordGenerator = new PasswordGenerator();
    private long lastSendMsgId = 0;
    private String currentMatchingPassword;
    private LoadBalancer loadBalancer;
    private int lastProcessPasswordIndex = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

    public Cluster getSeedNode() {
        return seedNode;
    }

    Node() {
        LOGGER.warn("node has been initialization begin ");
        initialize();
        LOGGER.warn("node has been initialized : "+memberID);
    }

    private void initialize() {
        seedNode = new ClusterImpl().config(clusterConfig -> clusterConfig.memberAlias("PWM_" + TimeUtils.currentTimeMillis()))
                .config(opts -> opts.metadata(Collections.singletonMap("nodeID", nodeID)))
                .membership(membershipConfig -> membershipConfig.seedMembers(rootClusterAddress))
                .handler(cluster -> this)
                .startAwait();
        memberID = seedNode.member().alias();
        System.out.println("MemberIDs :: " + memberID);
        nodeID = Long.parseLong(memberID.split("_")[1]);
        messageHandler = new MessageHandler(seedNode, nodeID);
        loadBalancer = new LoadBalancer(messageHandler);
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
        System.out.println("Member ID :: " + memberID + " :: " + event);
        if (nodeType == NodeType.MASTER && event.isAdded()) {
            requestActiveNodeDetails();
            System.out.println("Master Node Terminated and added");
        }

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
            onTimer(((TimerResponse) msg).getTimerID());
        } else if (msg instanceof HBMsg) {
            hbMiscount.set(0);
        } else if (msg instanceof LeaderElectionResultMsg) {
            LeaderElectionResultMsg resultMsg = (LeaderElectionResultMsg) msg;
            masterNodeAddress = resultMsg.getMasterAddress();
            clusterStatus.set(ClusterStatus.LEADER_ELECTED);
            nodeType = NodeType.SLAVE;
            System.out.println(memberID + " Leader Response Received");
        } else if (msg instanceof ResponseActiveNodeData) {
            System.out.println("Active Node List received");
            ResponseActiveNodeData activeNodeData = (ResponseActiveNodeData) msg;
            currentMatchingPassword = passwordList.get(lastProcessPasswordIndex);
            loadBalancer.publishNodeSchedule(activeNodeData.getActiveNodeList());
        } else if (msg instanceof NodeScheduleRequestMsg) {
            if (nodeType == NodeType.SLAVE) {
                NodeScheduleRequestMsg requestMsg = (NodeScheduleRequestMsg) msg;
                passwordGenerator.setCharset(requestMsg.getCharset());
                handlePasswordResponse(true, null);
            }
        } else if (msg instanceof PasswordMatchingData) {
            PasswordMatchingData matchingData = (PasswordMatchingData) msg;
            LOGGER.info(MessageFormat.format("Password suggestion receiving from : {0} :: {1}", matchingData.getMemberID(), matchingData.getPassword()));
            if (matchingData.getPassword().equals(currentMatchingPassword)) {
                LOGGER.info("Password Matched :: " + currentMatchingPassword + " : node " + matchingData.getMemberID());
                messageHandler.sendReplyToAll(new PasswordMatchingDataResponse(matchingData.getMsgID(), PasswordResponseType.MATCHED));
                lastProcessPasswordIndex++;
                requestActiveNodeDetails();
            } else {
                messageHandler.sendReplyToAll(new PasswordMatchingDataResponse(matchingData.getMsgID(), PasswordResponseType.UNMATCHED));
            }

        } else if (msg instanceof PasswordMatchingDataResponse) {
            hbMiscount.set(0);
            handlePasswordResponse(false, (PasswordMatchingDataResponse) msg);
        } else {
            System.out.println(memberID + " other Msg " + msg.toString());
        }
    }

    private void handlePasswordResponse(boolean isScheduleReq, PasswordMatchingDataResponse response) {
        if (isScheduleReq || response.getStatus() == PasswordResponseType.UNMATCHED) {
            lastSendMsgId = TimeUtils.currentTimeMillis();
            messageHandler.sendReply(masterNodeAddress, new PasswordMatchingData(passwordGenerator.generate(), memberID, lastSendMsgId));
        } else if (response.getStatus() == PasswordResponseType.MATCHED) {
            LOGGER.info("Password Matched :: " + response.toString());
        }

    }

    @Override
    public void onTimer(String timer) {
        if (timer.equalsIgnoreCase("HB-" + memberID)) {
            if (nodeType == NodeType.SLAVE) {
                if (hbMiscount.getAndIncrement() > maxHBExpiryCount && clusterStatus.get() == ClusterStatus.LEADER_ELECTED) {
                    System.out.println("Beginning new election");
                    startLeaderElection();
                }
            }
//            else {
//                messageHandler.sendReplyToAll(new HBMsg(TimeUtils.currentTimeMillis(), memberID));
//            }
            messageHandler.sendMessage(rootClusterAddress, new HBMsg(TimeUtils.currentTimeMillis(), memberID));
        } else if (timer.equalsIgnoreCase("LE-" + memberID) && !electionResponseReceived.get()) {
            LOGGER.info("New leader has been elected :: " + memberID);
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
        LOGGER.info(TimeUtils.getCurrentTime() + " Leader election response received : " + responseMsg.getMemberID() + " : current no : " + memberID);
        electionResponseReceived.set(true);
        if (leaderElectTimer != null) {
            leaderElectTimer.shutdown();
            leaderElectTimer = null;
        }

    }

    private void updateNodeState() {
        unhandled(() -> {
            nodeType = NodeType.MASTER;
            messageHandler.sendReplyToAll(new LeaderElectionResultMsg(seedNode.address(), masterNodeAddress != seedNode.address() ? Status.SELECTED : Status.UPDATED, memberID));
            masterNodeAddress = seedNode.address();
            passwordList = FileReader.readLines(ConfigReader.getEnvironmentConfigs("PASSWORD_PATH", "conf/password.txt"));
            LOGGER.info("Password list loaded");
            requestActiveNodeDetails();
        });
    }

    private void requestActiveNodeDetails() {
        messageHandler.sendMessage(rootClusterAddress, new RequestActiveNodeData(memberID));
    }
}

