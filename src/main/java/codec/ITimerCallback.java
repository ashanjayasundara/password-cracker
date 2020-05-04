package codec;

import java.util.Timer;

/**
 * @author ashan on 2020-05-03
 */
public interface ITimerCallback {
    public void onTimer(String timer) throws Exception;
}