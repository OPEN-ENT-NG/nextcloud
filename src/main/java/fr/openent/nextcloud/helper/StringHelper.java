package fr.openent.nextcloud.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;

public class StringHelper {

    private StringHelper() {
        throw new IllegalStateException("Utility StringHelper class");
    }

    public static String removeAccent(String value) {
        return Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * Remove first character from the string if the first character matches the firstChar param.
     * @param firstChar     First character to check.
     * @param string        The string you want to remove the character from.
     * @return              The string without first character.
     */
    public static String removeFirstChar(String firstChar, String string) {
        return string.startsWith(firstChar) ? string.substring(1) : string;
    }

    /**
     * Encode URL for Nextcloud server
     * @param url   Default URL
     * @return      Modified URL
     */
    public static String encodeUrlForNc(String url) {
        url = removeFirstChar("/", url);
        try {
            url = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url = url.replace("%2F", "/")
                .replace("+", "%20");
        return url;
    }
}
