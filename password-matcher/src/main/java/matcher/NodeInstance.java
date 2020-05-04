package matcher;

import java.util.concurrent.CompletableFuture;

import static utils.ExceptionHandler.unhandled;

/**
 * @author ashan on 2020-05-04
 */
public class NodeInstance {
    public static void createNodeInstance() {
        CompletableFuture.runAsync(() -> unhandled(Node::new));
    }
}
