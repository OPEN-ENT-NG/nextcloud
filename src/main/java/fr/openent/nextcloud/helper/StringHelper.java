package fr.openent.nextcloud.helper;

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
}
