package message;

import codec.DynamicMsg;
import io.scalecube.net.Address;

import java.util.List;

/**
 * @author ashan on 2020-05-09
 */
public class ResponseActiveNodeData extends DynamicMsg {
    private List<Address> activeNodeList;

    public ResponseActiveNodeData(List<Address> activeNodeList) {
        this.activeNodeList = activeNodeList;
    }

    public List<Address> getActiveNodeList() {
        return activeNodeList;
    }
}

