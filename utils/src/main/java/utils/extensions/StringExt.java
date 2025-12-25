package utils.extensions;

public class StringExt {
    public static String limit(String str, int limit) {
        if (str.length() > limit)
            return str.substring(0, limit);
        else
            return str;
    }
}
