package codec;

import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.net.Address;
import utils.ConfigReader;

/**
 * @author ashan on 2020-05-03
 */
public class ClusterInitializer {
    private static ClusterInitializer clusterInitializer = null;
    private static Cluster rootCluster = null;

    private ClusterInitializer() {
        rootCluster = new ClusterImpl()
                .config(opts -> opts.metadataCodec(new MessageCodec()))
                .transport(opts -> opts.host(ConfigReader.getEnvironmentConfigs("SCALECUBE_CON_STR")).port(
                        ConfigReader.getEnvironmentConfigsAsInteger("SCALECUBE_PORT", "7788")
                ))
                .startAwait();
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

    public static Cluster getRootCluster() throws Exception {
        if (rootCluster == null) {
            initialize();
        }
        return rootCluster;
    }

}
