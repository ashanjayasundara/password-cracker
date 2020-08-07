package message;

import codec.DynamicMsg;

/**
 * @author ashan on 2020-05-09
 */
public class PasswordMatchingDataResponse extends DynamicMsg {
    private long msgID;
    private PasswordResponseType status;

    public PasswordMatchingDataResponse(long msgID, PasswordResponseType status) {
        this.msgID = msgID;
        this.status = status;
    }

    public long getMsgID() {
        return msgID;
    }

    public PasswordResponseType getStatus() {
        return status;
    }
}
