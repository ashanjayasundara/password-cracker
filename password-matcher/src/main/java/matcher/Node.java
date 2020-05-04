package matcher;

import codec.ClusterInitializer;
import codec.ITimerCallback;
import codec.NodeTimer;
import com.google.gson.Gson;
import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.ClusterMessageHandler;
import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.net.Address;
import message.HBMsg;
import message.MessageParser;
import message.NodeType;
import utils.ConfigReader;
import utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ashan on 2020-05-03
 */
public class Node implements ClusterMessageHandler, ITimerCallback {
    private static long nodeID;
    private Cluster seedNode;
    private Cluster root = ClusterInitializer.getRootCluster();
    private NodeType nodeType = NodeType.SLAVE;
    private Gson gson = new Gson();
    private List<AutoCloseable> resourceManager = new ArrayList<>();
    private int hbInterval =  ConfigReader.getEnvironmentConfigsAsInteger("NODE_HB_INTERVAL","5");
    private int maxHBExpiryCount = ConfigReader.getEnvironmentConfigsAsInteger("NODE_MAX_HB_EXPIRE_COUNT","3");

    private NodeTimer hbTimer;

    public Cluster getSeedNode() {
        return seedNode;
    }

    public Node() throws Exception {
        initialize();
    }

    private void initialize() throws Exception {
        nodeID = TimeUtils.currentTimeMillis();
        seedNode = new ClusterImpl().config(clusterConfig -> clusterConfig.memberAlias("PWM_" + nodeID))
                .config(opts -> opts.metadata(Collections.singletonMap("nodeID", nodeID)))
                .membership(membershipConfig -> membershipConfig.seedMembers(Address.create(ConfigReader.getEnvironmentConfigs("SCALECUBE_CON_STR"),
                        ConfigReader.getEnvironmentConfigsAsInteger("SCALECUBE_PORT", "7788"))))
                .transport(transportConfig -> transportConfig.host(ConfigReader.getEnvironmentConfigs("SCALECUBE_CON_STR")))
                .handler(cluster -> this)
                .startAwait();
        hbTimer = new NodeTimer("HB-" + nodeID, true, hbInterval, this);
        resourceManager.add(hbTimer);
    }

    @Override
    public void onGossip(Message gossip) {
        Object msg = MessageParser.parse(gossip);
        if (msg instanceof HBMsg) {
            System.out.println("HBBBBBBBB");
        }


        System.out.println("Gossip Message :: " + seedNode.metadata() + " :: " + gossip.toString());
    }

    @Override
    public void onMembershipEvent(MembershipEvent event) {
        System.out.println("Membership Event :: " + event.toString());
    }

    @Override
    public void onMessage(Message message) {
        Object msg = MessageParser.parse(message);
    }

    @Override
    public void onTimer(String timer) throws Exception {
        System.out.println("Node Received Timer : " + timer);
    }

    public void terminate() throws Exception {
        if (seedNode != null) {
            seedNode.shutdown();
        }
        resourceManager.forEach(x -> {
            try {
                x.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
