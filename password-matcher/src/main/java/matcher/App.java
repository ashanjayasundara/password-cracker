package matcher;

import codec.ClusterInitializer;

/**
 * @author ashan on 2020-05-10
 */
public class App {
    public static void main(String[] args) throws Exception {
        if(args[0].equalsIgnoreCase("SYSMAN")){
            ClusterInitializer.getRootCluster();
        }else {
            int nodeCount = Integer.parseInt(args[0]);
            for(int i=0;i<nodeCount;i++){
                NodeInstance.createNodeInstance();
                System.out.println("Node created : "+i);
                Thread.sleep(1000);
            }
        }
        Thread.currentThread().join();
    }
}
