package message;

import codec.DynamicMsg;

/**
 * @author ashan on 2020-05-09
 */
public class NodeScheduleRequestMsg extends DynamicMsg {
    private String charset;

    public NodeScheduleRequestMsg(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }
}
