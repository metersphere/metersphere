package io.metersphere.api.service.utils;

import io.metersphere.base.mapper.ProjectApplicationMapper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class ShareUtill {

    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    private static final String UNIT_HOUR = "H";
    private static final String UNIT_DAY = "D";
    private static final String UNIT_MONTH = "M";
    private static final String UNIT_YEAR = "Y";

    public static long getTimeMills(String expr) {
        LocalDateTime localDateTime  = LocalDateTime.of(LocalDate.now(), LocalTime.now().withMinute(0).withSecond(0).withNano(0));
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
        if(StringUtils.equals(unit,UNIT_HOUR)){
            date = localDateTime.minusHours(quantity);
        } else if (StringUtils.equals(unit, UNIT_DAY)) {
            date = localDateTime.minusDays(quantity);
        } else if (StringUtils.equals(unit, UNIT_MONTH)) {
            date = localDateTime.minusMonths(quantity);
        } else if (StringUtils.equals(unit, UNIT_YEAR)) {
            date = localDateTime.minusYears(quantity);
        }
        return date;
    }
}
