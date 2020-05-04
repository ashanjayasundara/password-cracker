package message;

import codec.DynamicMsg;

/**
 * @author ashan on 2020-05-03
 */
public class HBMsg extends DynamicMsg {
    private long publishTime;
    private String masterID;

    public HBMsg(long publishTime, String masterID) {
        _msgType = this.getClass().getCanonicalName();
        this.publishTime = publishTime;
        this.masterID = masterID;
        _msgType = this.getClass().getCanonicalName();
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public String getMasterID() {
        return masterID;
    }

    public void setMasterID(String masterID) {
        this.masterID = masterID;
    }

}
