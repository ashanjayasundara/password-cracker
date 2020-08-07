package message;

import codec.DynamicMsg;

/**
 * @author ashan on 2020-05-09
 */
public class RequestActiveNodeData extends DynamicMsg {
    private String memberID;

    public RequestActiveNodeData(String memberID) {
        this.memberID = memberID;
    }

    public String getMemberID() {
        return memberID;
    }
}
