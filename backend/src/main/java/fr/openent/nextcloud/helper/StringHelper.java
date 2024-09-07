package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.core.constants.Field;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;

public class StringHelper {
    private static final Logger log = LoggerFactory.getLogger(StringHelper.class);
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
        String subUrl = removeFirstChar(Field.URL_DELIMITER, url);
        try {
            subUrl = URLEncoder.encode(subUrl, Field.UTF8);
        } catch (UnsupportedEncodingException e) {
            String messageToFormat = "[Nextcloud@::encodeUrlForNc] Error while encoding URL : %s";
            log.error(String.format(messageToFormat, e.getMessage()));
            return subUrl;
        }
        subUrl = subUrl.replace(Field.ASCII_SLASH, Field.URL_DELIMITER)
                .replace(Field.PLUS_SIGN, Field.ASCIISPACE);
        return subUrl;
    }

    /**
     * Decode URL for Nextcloud server
     * @param url   Default URL
     * @return      Modified URL
     */
    public static String decodeUrlForNc(String url) {
        String subUrl = removeFirstChar(Field.URL_DELIMITER, url);
        try {
            subUrl = URLDecoder.decode(subUrl, Field.UTF8);
        } catch (UnsupportedEncodingException e) {
            String messageToFormat = "[Nextcloud@::decodeUrlForNc] Error while decoding URL : %s";
            log.error(String.format(messageToFormat, e.getMessage()));
            return subUrl;
        }
        return subUrl;
    }
}
