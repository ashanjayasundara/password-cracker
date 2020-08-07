package common;

/**
 * @author ashan on 2020-05-09
 */
public class PasswordGenerator {
    private String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String generate() {
        int charsetLength = charset.length();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * charsetLength);
            buffer.append(charset.charAt(index));
        }
        return buffer.toString();
    }
}
