package message;

import codec.DynamicMsg;

/**
 * @author ashan on 2020-05-09
 */
public class PasswordMatchingData extends DynamicMsg {
    private String password;
    private String memberID;
    private long msgID;

    public long getMsgID() {
        return msgID;
    }

    public PasswordMatchingData(String password, String memberID, long msgID) {
        this.password = password;
        this.memberID = memberID;
        this.msgID = msgID;
    }

    public String getPassword() {
        return password;
    }

    public String getMemberID() {
        return memberID;
    }
}
