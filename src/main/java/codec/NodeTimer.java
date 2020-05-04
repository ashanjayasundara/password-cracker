package codec;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static utils.ExceptionHandler.unhandled;

/**
 * @author ashan on 2020-05-03
 */
public class NodeTimer extends TimerTask implements AutoCloseable{

    private String name;
    private boolean isRepeatable;
    private int interval;
    private ITimerCallback callback;
    private Timer timer;

    public NodeTimer(String name, boolean isRepeatable, int interval, ITimerCallback callback) {
        this.name = name;
        this.isRepeatable = isRepeatable;
        this.interval = interval;
        this.callback = callback;
        timer = new Timer();

        if (isRepeatable) {
            timer.scheduleAtFixedRate(this, 0, interval * 1000);
        } else {
            timer.schedule(this, 0, interval * 1000);
        }
    }

    public void shutdown() throws Exception {
        timer.cancel();
    }


    @Override
    public void run() {
        unhandled(()-> callback.onTimer(name));
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }
}
