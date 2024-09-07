package fr.openent.nextcloud.helper;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateHelper.class);

    public static final String NEXTCLOUD_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    public static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static SimpleDateFormat getNextcloudSimpleDateFormat() {
        return new SimpleDateFormat(NEXTCLOUD_FORMAT);
    }

    public static SimpleDateFormat getUTCSimpleDateFormat() {
        return new SimpleDateFormat(UTC_FORMAT);
    }

    public static Date parseDate(String dateString, String format) {
        Date date = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            LOGGER.error("[Nextcloud@DateHelper::parseDate] Error when casting date: ", e);
        }

        return date;
    }

    public static String getDateString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
}
