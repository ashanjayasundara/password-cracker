package codec;

import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.ClusterMessageHandler;
import io.scalecube.cluster.Member;
import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.net.Address;
import message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ClusterStatus;
import utils.ConfigReader;
import utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ashan on 2020-05-03
 */
public class ClusterInitializer implements ClusterMessageHandler, ITimerCallback, AutoCloseable {
    private static ClusterInitializer clusterInitializer = null;
    private static Cluster rootCluster = null;
    private static final NodeType nodeType = NodeType.ROOT;
    private static long currentNodeID = TimeUtils.currentTimeMillis();
    private final static ReentrantLock counterLock = new ReentrantLock(true);
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterInitializer.class);
    private static Address leaderAddress;
    private ClusterStatus clusterStatus = ClusterStatus.WAIT_FOR_LEADER;
    private final static ReentrantLock leaderLock = new ReentrantLock(true);
    private List<AutoCloseable> resourceManager = new ArrayList<>();
    private MessageHandler messageHandler;
    private NodeTimer clusterTimer;
    private Map<String, Member> seedMemberMap = new ConcurrentHashMap<String, Member>();

    private ClusterInitializer() {
        LOGGER.info("Initializing Root Cluster");
        rootCluster = new ClusterImpl()
                .config(opts -> opts.metadataCodec(new MessageCodec()).memberAlias(ConfigReader.getEnvironmentConfigs("SCALECUBE_ENV_NAME") + "_" + currentNodeID))
                .transport(opts -> opts.host(ConfigReader.getEnvironmentConfigs("SCALECUBE_CON_STR")).port(
                        ConfigReader.getEnvironmentConfigsAsInteger("SCALECUBE_PORT", "7788")
                )).handler(cluster -> this)
                .startAwait();
        messageHandler = new MessageHandler(rootCluster, currentNodeID);
        clusterTimer = new NodeTimer(rootCluster, "CLUSTER_EVENT_SCHEDULER", true, 5, this).start();
        LOGGER.error("Root Cluster Initialization Completed");
    }

    private static void initialize() {
        synchronized (ClusterInitializer.class) {
            clusterInitializer = new ClusterInitializer();
        }
    }

    public static ClusterInitializer getInstance() throws Exception {
        if (clusterInitializer == null)
            initialize();
        return clusterInitializer;
    }

    public static Cluster getRootCluster() {
        if (rootCluster == null) {
            initialize();
        }
        return rootCluster;
    }

    public static long getCurrentNodeID() {
        incrementNodeIndex();
        return currentNodeID;
    }

    private static void incrementNodeIndex() {
        counterLock.lock();
        try {
            currentNodeID++;
        } finally {
            counterLock.unlock();
        }
    }

    @Override
    public void onGossip(Message gossip) {
        System.out.println("Root : " + gossip.data());
    }

    @Override
    public void onMessage(Message message) {
        Object msg = MessageParser.parse(message);
        if (msg instanceof LeaderElectionResponseMsg) {
            clusterStatus = ClusterStatus.PENDING_ELECTION;
        } else if (msg instanceof TimerResponse) {
            this.onTimer(((TimerResponse) msg).getTimerID());
        }

    }

    @Override
    public void onMembershipEvent(MembershipEvent event) {
        if (event.isAdded()) {
            seedMemberMap.put(event.member().alias(), event.member());
        } else if (event.isLeaving() || event.isRemoved()) {
            seedMemberMap.remove(event.member().alias());
        }

    }

    @Override
    public void onTimer(String timer) {
        if (timer.equalsIgnoreCase("CLUSTER_EVENT_SCHEDULER")) {
            if (clusterStatus == ClusterStatus.WAIT_FOR_LEADER && seedMemberMap.size() > 2) {
                initiateLeaderElection();
            }
        }
    }

    @Override
    public void close() throws Exception {

    }


    private void initiateLeaderElection() {
        Member member = new ArrayList<>(seedMemberMap.values()).get(0);
        messageHandler.sendMessage(member.address(), new LeaderElectionRequestMsg(rootCluster.address(), rootCluster.member().alias()));
        clusterStatus = ClusterStatus.PENDING_ELECTION;
    }
}
