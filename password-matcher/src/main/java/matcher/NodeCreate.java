package matcher;

/**
 * @author ashan on 2020-05-09
 */
public class NodeCreate {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Node");
        NodeInstance.createNodeInstance();
        Thread.currentThread().join();
    }
}
