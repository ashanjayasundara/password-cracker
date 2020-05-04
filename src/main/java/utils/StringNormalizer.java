package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ashan on 2020-05-01
 */
public class StringNormalizer {
    public static String normalize(String text) {
        if (text == null) {
            return null;
        }
        String ret = text;
        {
            Pattern pattern = Pattern.compile("(\\$\\{)(.*?)(\\})");
            Matcher matcher = pattern.matcher(text);

            List<String> listMatches = new ArrayList<>();

            while (matcher.find()) {
                listMatches.add(matcher.group(2));
            }

            for (String s : listMatches) {
                String val = System.getenv(s);
                if (val != null) {
                    val = val.replaceAll("\\\\", "\\\\\\\\");
                    ret = ret.replaceAll("\\$\\{" + s + "\\}", val);
                }
            }
        }
        {
            Pattern pattern = Pattern.compile("(%\\{)(.*?)(\\})");
            Matcher matcher = pattern.matcher(text);

            List<String> listMatches = new ArrayList<>();

            while (matcher.find()) {
                listMatches.add(matcher.group(2));
            }

            for (String s : listMatches) {
                String val = System.getProperty(s);
                if (val != null) {
                    val = val.replaceAll("\\\\", "\\\\\\\\");
                    ret = ret.replaceAll("%\\{" + s + "\\}", val);
                }
            }
        }
        return ret;
    }
}
