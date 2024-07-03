package io.metersphere.sdk.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class DateUtils {
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";


    public static Date getDate(String dateString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return dateFormat.parse(dateString);
    }

    public static Date getTime(String timeString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.parse(timeString);
    }

    public static String getDateString(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return dateFormat.format(date);
    }

    public static String getDateString(long timeStamp) throws Exception {
        return getDataStr(timeStamp);
    }

    public static String getTimeString(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(date);
    }

    public static String getTimeString(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(timeStamp);
    }

    public static String getTimeStr(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(timeStamp);
    }

    public static String getDataStr(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return dateFormat.format(timeStamp);
    }


    public static Date dateSum(Date date, int countDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, countDays);

        return calendar.getTime();
    }

    public static Date dateSum(Date date, int countUnit, int calendarType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendarType, countUnit);

        return calendar.getTime();
    }

    /**
     * 获取入参日期所在周的周一周末日期。 日期对应的时间为当日的零点
     *
     * @return Map<String, String>(2); key取值范围：firstTime/lastTime
     */
    public static Map<String, Date> getWeedFirstTimeAndLastTime(Date date) throws Exception{
        Map<String, Date> returnMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int dayWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayWeek == 1) {
            dayWeek = 8;
        }
        calendar.add(Calendar.DATE, calendar.getFirstDayOfWeek() - dayWeek);
        //第一天的时分秒是 00:00:00 这里直接取日期，默认就是零点零分
        Date thisWeekFirstTime = getDate(getDateString(calendar.getTime()));

        //计算七天过后的日期
        calendar.add(Calendar.DATE, 7);
        Date nextWeekFirstDay = getDate(getDateString(calendar.getTime()));
        Date thisWeekLastTime = getTime(getTimeString(nextWeekFirstDay.getTime() - 1));

        returnMap.put("firstTime", thisWeekFirstTime);
        returnMap.put("lastTime", thisWeekLastTime);
        return returnMap;

    }

    /**
     * 获取当前时间或者当前时间+- 任意天数 时间的时间戳
     *
     * @param countDays
     * @return
     */
    public static Long getTimestamp(int countDays) {
        Date now = new Date();
        return dateSum(now, countDays).getTime() / 1000 * 1000;
    }

    public static Long getTimestamp(String dateString) throws Exception {
        return getDate(dateString).getTime();
    }

    /**
     * 获取当天的起始时间Date
     *
     * @param time 指定日期  例： 2020-12-13 06:12:42
     * @return 当天起始时间 例： 2020-12-13 00:00:00
     * @throws Exception
     */
    public static Date getDayStartTime(Date time) throws Exception {
        return getDate(getDateString(time));
    }

    public static long getDailyStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getDailyEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}
