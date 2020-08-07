import codec.ClusterInitializer;
import matcher.NodeInstance;
import utils.ConfigReader;
import utils.FileReader;

/**
 * @author ashan on 2020-05-03
 */
public class TestCluster {
    public static void main(String[] args) throws Exception {
        ClusterInitializer.getRootCluster();
        System.out.println("Root node created");

        NodeInstance.createNodeInstance();
        Thread.sleep(250);
        NodeInstance.createNodeInstance();
        Thread.sleep(250);
        NodeInstance.createNodeInstance();
        Thread.sleep(250);
        NodeInstance.createNodeInstance();
//        Thread.sleep(250);
//        NodeInstance.createNodeInstance();
//        Thread.sleep(250);
//        NodeInstance.createNodeInstance();
//        Thread.sleep(250);
//        NodeInstance.createNodeInstance();

        Thread.currentThread().join();

//        FileReader.readLines(ConfigReader.getEnvironmentConfigs("PASSWORD_PATH")).forEach(x -> {
//            System.out.println("Read lines :: " + x);
//        });
    }
}
