package codec;

import io.scalecube.cluster.Cluster;
import message.MessageParser;
import message.TimerResponse;
import reactor.core.publisher.Flux;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static utils.ExceptionHandler.unhandled;

/**
 * @author ashan on 2020-05-03
 */
public class NodeTimer extends TimerTask implements AutoCloseable {

    private String name;
    private boolean isRepeatable;
    private int interval;
    private ITimerCallback callback;
    private Timer timer;
    private Cluster cluster;
    private boolean nodeScheduleTimer = true;

    public NodeTimer(Cluster cluster, String name, boolean isRepeatable, int interval, ITimerCallback callback) {
        this.name = name;
        this.isRepeatable = isRepeatable;
        this.interval = interval;
        this.callback = callback;
        this.cluster = cluster;
        timer = new Timer();
    }

    public NodeTimer(Cluster cluster, String name, boolean isRepeatable, int interval, boolean nodeScheduleTimer, ITimerCallback callback) {
        this.name = name;
        this.isRepeatable = isRepeatable;
        this.interval = interval;
        this.callback = callback;
        this.cluster = cluster;
        this.nodeScheduleTimer = nodeScheduleTimer;
        timer = new Timer();
    }

    public NodeTimer start() {
        if (isRepeatable) {
            timer.scheduleAtFixedRate(this, 0, interval * 1000);
        } else {
            timer.schedule(this, 0, interval * 1000);
        }
//        System.out.println("Timer Started " + name);
        return this;
    }

    public void shutdown() {
//        System.out.println("Timer Canceled " + name);
        timer.cancel();
    }

    @Override
    public void run() {
        unhandled(() -> {
            if (nodeScheduleTimer)
                Flux.fromIterable(cluster.members().stream().filter(x -> x.address().equals(cluster.address())).collect(Collectors.toSet())).flatMap(
                        member -> cluster.send(member, MessageParser.serialized(new TimerResponse(name)))
                ).subscribe(null, Throwable::printStackTrace);
            else {
                callback.onTimer(name);
            }
            if (!isRepeatable)
                shutdown();
        });
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }
}
