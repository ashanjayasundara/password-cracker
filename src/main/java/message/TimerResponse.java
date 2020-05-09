package message;

import codec.DynamicMsg;

/**
 * @author ashan on 2020-05-09
 */
public class TimerResponse extends DynamicMsg {
    private String timerID;

    public TimerResponse(String timerID) {
        this.timerID = timerID;
    }

    public String getTimerID() {
        return timerID;
    }
}
