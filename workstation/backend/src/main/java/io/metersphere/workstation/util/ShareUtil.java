package io.metersphere.workstation.util;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ShareUtil {

    private static final String UNIT_HOUR = "H";
    private static final String UNIT_DAY = "D";
    private static final String UNIT_MONTH = "M";
    private static final String UNIT_YEAR = "Y";

    /**
     * return time + expr
     *
     * @param time
     * @param expr
     * @return
     */
    public static long getTimeMills(long time, String expr) {
        Instant instant = Instant.ofEpochMilli(time);
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        long timeMills = 0;
        LocalDateTime date = exprToLocalDateTime(localDateTime, expr);
        if (date != null) {
            timeMills = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return timeMills;
    }

    public static LocalDateTime exprToLocalDateTime(LocalDateTime localDateTime, String expr) {
        LocalDateTime date = null;
        String unit = expr.substring(expr.length() - 1);
        int quantity = Integer.parseInt(expr.substring(0, expr.length() - 1));
        if (StringUtils.equals(unit, UNIT_HOUR)) {
            date = localDateTime.plusHours(quantity);
        } else if (StringUtils.equals(unit, UNIT_DAY)) {
            date = localDateTime.plusDays(quantity);
        } else if (StringUtils.equals(unit, UNIT_MONTH)) {
            date = localDateTime.plusMonths(quantity);
        } else if (StringUtils.equals(unit, UNIT_YEAR)) {
            date = localDateTime.plusYears(quantity);
        }
        return date;
    }
}
