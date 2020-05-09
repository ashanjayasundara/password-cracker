package utils;

import org.ini4j.Profile;
import org.ini4j.Wini;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ashan on 2020-05-01
 */
public class ConfigReader {
    private static Map<String, String> environmentConfigs;

    static {
        ExceptionHandler.unhandled(() -> {
            environmentConfigs = normalizeConfigs(read("conf/envmanconf.ini"));
            InetAddress inetAddress = InetAddress.getLocalHost();
            environmentConfigs.put("SCALECUBE_CON_STR",inetAddress.getHostAddress());
        });
    }

    static Map<String, String> read(String fileName) throws Exception {
        Map<String, String> configs = new HashMap<>();
        Wini ini = new Wini(new File(fileName));
        for (String key : ini.keySet()) {
            List<Profile.Section> sections = ini.getAll(key);
            for (Profile.Section sec : sections) {
                for (String key2 : sec.keySet()) {
                    String val = sec.get(key2);
                    configs.put(key + "_" + key2, val);
                }
            }
        }
        return configs;
    }

    static Map<String, String> normalizeConfigs(Map<String, String> config) {
        Map<String, String> normalized = new HashMap<>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            normalized.put(entry.getKey(), StringNormalizer.normalize(entry.getValue()));
        }
        return normalized;
    }

    public static Map<String, String> getDefaultConfigMap() {
        return environmentConfigs;
    }

    public static String getEnvironmentConfigs(String key) {
        return getEnvironmentConfigs(key, "");
    }

    public static int getEnvironmentConfigsAsInteger(String key) {
        return getEnvironmentConfigsAsInteger(key, "0");
    }

    public static int getEnvironmentConfigsAsInteger(String key, String defaultValue) {
        return Integer.parseInt(getEnvironmentConfigs(key, defaultValue));
    }

    public static String getEnvironmentConfigs(String key, String defaultValue) {
        if (environmentConfigs.containsKey(key))
            return environmentConfigs.get(key);
        return defaultValue;
    }
}
