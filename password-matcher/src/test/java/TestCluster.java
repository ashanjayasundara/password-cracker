import codec.ClusterInitializer;
import matcher.NodeInstance;

/**
 * @author ashan on 2020-05-03
 */
public class TestCluster {
    public static void main(String[] args) throws Exception {
        ClusterInitializer.getRootCluster();

        NodeInstance.createNodeInstance();
        NodeInstance.createNodeInstance();
        NodeInstance.createNodeInstance();
        NodeInstance.createNodeInstance();
        NodeInstance.createNodeInstance();
        NodeInstance.createNodeInstance();
        NodeInstance.createNodeInstance();

        Thread.currentThread().join();


    }
}
