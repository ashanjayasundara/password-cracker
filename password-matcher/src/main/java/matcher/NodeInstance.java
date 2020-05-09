package matcher;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ashan on 2020-05-04
 */
public class NodeInstance {
    private static ExecutorService scheduleService = Executors.newFixedThreadPool(100, new ThreadFactoryBuilder()
            .setNameFormat("password-matching-node-%d")
            .setThreadFactory(Executors.defaultThreadFactory())
            .build());

    public static void createNodeInstance() throws Exception {
        CompletableFuture.runAsync((Node::new), scheduleService);
    }
}
