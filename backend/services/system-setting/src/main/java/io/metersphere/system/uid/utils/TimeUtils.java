package io.metersphere.system.uid.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateUtils provides date formatting, parsing
 */
public abstract class TimeUtils extends org.apache.commons.lang3.time.DateUtils {
    /**
     * Patterns
     */
    public static final String DAY_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";


    /**
     * Parse date by 'yyyy-MM-dd' pattern
     */
    public static Date parseByDayPattern(String str) {
        return parseDate(str, DAY_PATTERN);
    }

    /**
     * Parse date without Checked exception
     *
     * @throws RuntimeException when ParseException occurred
     */
    public static Date parseDate(String str, String pattern) {
        try {
            return parseDate(str, new String[]{pattern});
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Format date by 'yyyy-MM-dd HH:mm:ss' pattern
     */
    public static String formatByDateTimePattern(Date date) {
        return DateFormatUtils.format(date, DATETIME_PATTERN);
    }


    public static String getDataStr(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DAY_PATTERN);
        return dateFormat.format(timeStamp);
    }
}
