package io.metersphere.system.utils;


import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

public class ScheduleUtils {
    /**
     * 获取下次执行时间（getFireTimeAfter，也可以下下次...）
     *
     * @param cron cron表达式
     * @return 下次执行时间
     */
    public static Long getNextTriggerTime(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return null;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        Date time0 = trigger.getStartTime();
        Date time1 = trigger.getFireTimeAfter(time0);
        return time1 == null ? 0 : time1.getTime();
    }
}
