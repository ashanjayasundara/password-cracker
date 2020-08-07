package message;

import codec.DynamicMsg;
import io.scalecube.net.Address;

/**
 * @author ashan on 2020-05-09
 */
public class LeaderElectionResultMsg extends DynamicMsg {
    public enum Status {
        SELECTED,
        UPDATED;

    }

    private Address masterAddress;
    private Status status;
    private String memberID;

    public LeaderElectionResultMsg(Address masterAddress, Status status, String memberID) {
        this.masterAddress = masterAddress;
        this.status = status;
        this.memberID = memberID;
    }

    public String getMemberID() {
        return memberID;
    }

    public LeaderElectionResultMsg(Address masterAddress) {
        this.masterAddress = masterAddress;
        this.status = Status.SELECTED;
    }

    public Address getMasterAddress() {
        return masterAddress;
    }

    public Status getStatus() {
        return status;
    }
}
