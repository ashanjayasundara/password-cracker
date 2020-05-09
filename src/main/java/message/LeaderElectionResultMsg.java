package message;

import codec.DynamicMsg;
import io.scalecube.net.Address;

/**
 * @author ashan on 2020-05-09
 */
public class LeaderElectionResultMsg extends DynamicMsg {
    public enum Status {
        SELECTED,
        PENDING;

    }

    private Address masterAddress;
    private Status status;

    public LeaderElectionResultMsg(Address masterAddress, Status status) {
        this.masterAddress = masterAddress;
        this.status = status;
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
