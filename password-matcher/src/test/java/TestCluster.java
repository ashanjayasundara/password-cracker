import codec.ClusterInitializer;
import com.google.gson.Gson;
import io.scalecube.cluster.transport.api.Message;
import matcher.NodeInstance;
import message.HBMsg;

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

        Gson gson = new Gson();
        gson.toJson(new HBMsg(15631, "asdasd"));
        Message greetingMsg = Message.fromData(

                "sdsdas asdas"
        );

        Thread.sleep(2000);
        ClusterInitializer.getRootCluster().spreadGossip(Message.fromData(new HBMsg(15631, "asdasd").toString()))
                .doOnError(System.err::println)
                .subscribe(null, Throwable::printStackTrace);

//
//        // Avoid exit main thread immediately ]:->

        Thread.currentThread().join();

        C
    }
}
