package fr.openent.nextcloud.helper;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class QuotaHelper {

    private QuotaHelper() { throw new IllegalStateException("Helper QuotaHelper class"); }

    /**
     * Returns a human-readable value byte (1.0 mb, 1.0Gb etc...)
     *
     * @param   bytes   long
     * @return  A formatted human-readable byte as a string
     */
    public static String humanReadableByteCount(long bytes) {
        final String characterIterator = "kMGTPE";
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator(characterIterator);
        //999950    would be 1000.0 kB instead of 1.0 MB
        //999950000 would be 1000.0 MB instead of 1.0 GB
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
