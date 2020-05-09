package message;

import codec.DynamicMsg;
import io.scalecube.net.Address;

/**
 * @author ashan on 2020-05-08
 */
public class LeaderElectionRequestMsg extends DynamicMsg {
    private Address leafMember;
    private String memberID;

    public String getMemberID() {
        return memberID;
    }

    public LeaderElectionRequestMsg(Address leafMember, String memberID) {
        this.leafMember = leafMember;
        this.memberID = memberID;
    }

    public Address getLeafMember() {
        return leafMember;
    }
}
