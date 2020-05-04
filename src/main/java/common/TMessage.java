package common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ashan on 2020-05-03
 */
public class TMessage {
    private final Map<String, String> attr;
    private final Object data;
    private final boolean ctrl;
    private final String key;

    protected TMessage(Object data, String key, boolean ctrl, Map<String, String> attr) {
        this.data = data;
        this.ctrl = ctrl;
        this.attr = attr;
        this.key = key;
    }

    public String attr(String key) {
        return this.attr.get(key);
    }

    protected void attr(String key, String value) {
        this.attr.put(key, value);
    }

    public Map<String, String> attr() {
        return attr;
    }

    public boolean isCtrl() {
        return ctrl;
    }

    public <T> T data() {
        return (T) data;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "TMessage{" + "attr=" + attr + ", data=" + data + ", ctrl=" + ctrl + ", key=" + key + '}';
    }

    public static TMessage create(Object data) {
        return builder().setData(data).build();
    }

    public static TMessage createCtrl(Object data) {
        return builder().setCtrl(true).setData(data).build();
    }

    public static TMessage create(Object data, String key1, String val1) {
        return builder().setData(data).attr(key1, val1).build();
    }

    public static TMessage create(Object data, String key, String key1, String val1) {
        return builder().setData(data).setKey(key).attr(key1, val1).build();
    }

    public static TMessage create(Object data, String key) {
        return builder().setData(data).setKey(key).build();
    }

    public static TMessage create(Object data, String key1, String val1, String key2, String val2) {
        return builder().setData(data).attr(key1, val1).attr(key2, val2).build();
    }

    public static TMessage create(Object data, String key, String key1, String val1, String key2, String val2) {
        return builder().setData(data).setKey(key).attr(key1, val1).attr(key2, val2).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Map<String, String> attr = new HashMap<>();
        private Object data = NullData.NULL_DATA;
        private boolean ctrl = false;
        private String key = null;

        private Builder() {
        }

        public Map<String, String> getAttr() {
            return attr;
        }

        public Object getData() {
            return data;
        }

        public Builder set(TMessage msg) {
            this.attr = msg.attr;
            this.data = msg.data;
            this.ctrl = msg.ctrl;
            this.key = msg.key;
            return this;
        }

        public Builder setCtrl(boolean ctrl) {
            this.ctrl = ctrl;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public boolean isCtrl() {
            return ctrl;
        }

        public void setAttr(Map<String, String> attr) {
            this.attr = attr;
        }

        public Builder setData(Object data) {
            this.data = data;
            return this;
        }

        public Builder attr(String key, String value) {
            this.attr.put(key, value);
            return this;
        }

        public TMessage build() {
            return new TMessage(data, key, ctrl, attr);
        }
    }
}
