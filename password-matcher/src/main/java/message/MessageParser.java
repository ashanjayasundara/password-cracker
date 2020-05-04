package message;

import codec.DynamicMsg;
import com.google.gson.Gson;
import io.scalecube.cluster.transport.api.Message;


/**
 * @author ashan on 2020-05-04
 */
public class MessageParser {
    private static Gson gson = new Gson();

    public static Object parse(Message message) {
        DynamicMsg dynamicMsg = gson.fromJson(message.data().toString(), DynamicMsg.class);
        try {
            Class aClass = Class.forName(dynamicMsg._msgType);
            return gson.fromJson(message.data().toString(), aClass);
        } catch (Exception e) {
            return dynamicMsg;
        }
    }
}
