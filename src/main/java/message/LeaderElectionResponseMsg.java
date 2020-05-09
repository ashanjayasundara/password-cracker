package message;

import codec.DynamicMsg;

/**
 * @author ashan on 2020-05-08
 */
public class LeaderElectionResponseMsg extends DynamicMsg {
    private String memberID;

    public LeaderElectionResponseMsg(String memberID) {
        this.memberID = memberID;
    }

    public String getMemberID() {
        return memberID;
    }
}
