package matcher;

import codec.MessageHandler;
import io.scalecube.net.Address;
import message.NodeScheduleRequestMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ashan on 2020-05-09
 */
public class LoadBalancer {
    private Map<Integer, String[]> loadConfigs = new HashMap<>();
    private int maxScheduleIndex;
    private MessageHandler messageHandler;

    LoadBalancer(MessageHandler messageHandler) {
        loadConfigs.put(1, new String[]{"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"});
        loadConfigs.put(2, new String[]{"ABCDEFGHIJKLMabcdefghijklm0123456789", "NOPQRSTUVWXYZnopqrstuvwxyz0123456789"});
        loadConfigs.put(3, new String[]{"ABCDEFGHIabcdefghi0123456789", "JKLMNOPQRjklmnopqr0123456789", "STUVWXYZstuvwxyz0123456789"});
        loadConfigs.put(4, new String[]{"ABCDEFabcdef0123456789", "GHIJKLghijkl0123456789", "MNOPQRmnopqr0123456789", "STUVWXYZstuvwxyz0123456789"});
        loadConfigs.put(5, new String[]{"ABCDEabcde0123456789", "FGHIJfghij0123456789", "KLMNOklmno0123456789", "PQRSTpqrst0123456789", "UVWXYZauvwxyz0123456789"});
        this.messageHandler = messageHandler;
        maxScheduleIndex = loadConfigs.size();
    }

    public void publishNodeSchedule(List<Address> activeNodeList) {
        int scheduleSchemeIndex = activeNodeList.size() >= maxScheduleIndex ? maxScheduleIndex : activeNodeList.size();
        String[] scheduleData = loadConfigs.get(scheduleSchemeIndex);
        for (int i = 0; i < activeNodeList.size(); i++) {
            int index = i % scheduleSchemeIndex;
            System.out.println("Schedule Index :: " + index);
            messageHandler.sendReply(activeNodeList.get(i),new NodeScheduleRequestMsg(scheduleData[index]));
        }
    }
}
