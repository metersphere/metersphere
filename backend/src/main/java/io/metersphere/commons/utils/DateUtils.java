package io.metersphere.commons.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class DateUtils {
    public static final String DATE_PATTERM = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";


    public static Date getDate(String dateString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERM);
        return dateFormat.parse(dateString);
    }
    public static Date getTime(String timeString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.parse(timeString);
    }

    public static String getDateString(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERM);
        return dateFormat.format(date);
    }

    public static String getDateString(long timeStamp) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERM);
        return dateFormat.format(timeStamp);
    }

    public static String getTimeString(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(date);
    }

    public static String getTimeString(long timeStamp) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(timeStamp);
    }

    public static String getTimeStr(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(timeStamp);
    }
    public static String getDataStr(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERM);
        return dateFormat.format(timeStamp);
    }


    public static Date dateSum (Date date,int countDays){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,countDays);

        return calendar.getTime();
    }

    public static Date dateSum (Date date,int countUnit,int calendarType){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendarType,countUnit);

        return calendar.getTime();
    }

    /**
     * 获取入参日期所在周的周一周末日期。 日期对应的时间为当日的零点
     *
     * @return Map<String, String>(2); key取值范围：firstTime/lastTime
     */
    public static Map<String, Date> getWeedFirstTimeAndLastTime(Date date) {
        Map<String, Date> returnMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        //Calendar默认一周的开始是周日。业务需求从周一开始算，所以要"+1"
        int weekDayAdd = 1;

        try {
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK));
            calendar.add(Calendar.DAY_OF_MONTH,weekDayAdd);

            //第一天的时分秒是 00:00:00 这里直接取日期，默认就是零点零分
            Date thisWeekFirstTime = getDate(getDateString(calendar.getTime()));

            calendar.clear();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
            calendar.add(Calendar.DAY_OF_MONTH,weekDayAdd);

            //最后一天的时分秒应当是23:59:59。 处理方式是增加一天计算日期再-1
            calendar.add(Calendar.DAY_OF_MONTH,1);
            Date nextWeekFirstDay = getDate(getDateString(calendar.getTime()));
            Date thisWeekLastTime = getTime(getTimeString(nextWeekFirstDay.getTime()-1));

            returnMap.put("firstTime", thisWeekFirstTime);
            returnMap.put("lastTime", thisWeekLastTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;

    }

    /**
     * 获取当前时间或者当前时间+- 任意天数 时间的时间戳
     * @param countDays
     * @return
     */
    public static Long getTimestamp(int countDays){
        Date now = new Date();
        return dateSum (now,countDays).getTime()/1000*1000;
    }

    public static void main(String[] args) throws Exception {
        Date now = new Date();
        long time = dateSum(now, -3).getTime()/1000*1000;
        long time1 = dateSum(now, 0).getTime()/1000*1000;
        Date afterDate = dateSum(now,-3);
        System.out.println(getTimeString(afterDate));
        /*System.out.println(getTimeString(now));
        Date afterDate = dateSum(now,-30,Calendar.DAY_OF_MONTH);
        System.out.println(getTimeString(afterDate));
        afterDate = dateSum(now,-17,Calendar.MONTH);
        System.out.println(getTimeString(afterDate));
        afterDate = dateSum(now,-20,Calendar.YEAR);
        System.out.println(getTimeString(afterDate));*/
        System.out.println(time);
        System.out.println(time1);
    }


    /**
     * 获取当天的起始时间Date
     * @param time  指定日期  例： 2020-12-13 06:12:42
     * @return  当天起始时间 例： 2020-12-13 00:00:00
     * @throws Exception
     */
    public static Date getDayStartTime(Date time) throws Exception {
        return getDate(getDateString(time));
    }
}
