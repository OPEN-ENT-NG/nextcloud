package fr.openent.nextcloud.helper;

public class QuotaHelper {

    private QuotaHelper() { throw new IllegalStateException("Helper QuotaHelper class"); }

    /**
     * Returns a human-readable value byte (1.0 mb, 1.0Gb etc...)
     *
     * @param   value   long
     * @return  A formatted human-readable byte as a string
     */
    public static String humanReadableByteCount(long value) {
        if (value < 1024) return value + " B";
        int z = (63 - Long.numberOfLeadingZeros(value)) / 10;
        return String.format("%.1f %sB", (double)value / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
