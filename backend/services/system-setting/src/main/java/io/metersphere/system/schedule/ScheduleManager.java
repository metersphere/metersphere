package io.metersphere.system.schedule;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Schedule;
import jakarta.annotation.Resource;
import org.quartz.*;

/**
 * @Description: 定时任务管理类
 */
public class ScheduleManager {

    @Resource
    private Scheduler scheduler;

    /**
     * 添加 simpleJob
     */
    public void addSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> cls, int repeatIntervalTime, JobDataMap jobDataMap)
            throws SchedulerException {

        JobBuilder jobBuilder = JobBuilder.newJob(cls).withIdentity(jobKey);
        if (jobDataMap != null) {
            jobBuilder.usingJobData(jobDataMap);
        }

        SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(repeatIntervalTime).repeatForever())
                .startNow().build();

        scheduler.scheduleJob(jobBuilder.build(), trigger);
    }

    /**
     * 添加 cronJob
     */
    public void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> jobClass, String cron, JobDataMap jobDataMap) {
        try {
            LogUtils.info("addCronJob: " + triggerKey.getName() + "," + triggerKey.getGroup());
            JobBuilder jobBuilder = JobBuilder.newJob(jobClass).withIdentity(jobKey);
            if (jobDataMap != null) {
                jobBuilder.usingJobData(jobDataMap);
            }

            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            triggerBuilder.withIdentity(triggerKey);
            triggerBuilder.startNow();
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();
            scheduler.scheduleJob(jobBuilder.build(), trigger);

        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("定时任务配置异常: " + e.getMessage());
        }
    }

    public void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> jobClass, String cron) {
        addCronJob(jobKey, triggerKey, jobClass, cron, null);
    }

    /**
     * 修改 cronTrigger
     */
    public void modifyCronJobTime(TriggerKey triggerKey, String cron) throws SchedulerException {

        LogUtils.info("modifyCronJobTime: " + triggerKey.getName() + "," + triggerKey.getGroup());
        try {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }

            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(cron)) {
                /* 方式一 ：调用 rescheduleJob 开始 */
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();// 触发器
                triggerBuilder.withIdentity(triggerKey);// 触发器名,触发器组
                triggerBuilder.startNow(); // 立即执行
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron)); // 触发器时间设定
                trigger = (CronTrigger) triggerBuilder.build(); // 创建Trigger对象
                scheduler.rescheduleJob(triggerKey, trigger); // 修改一个任务的触发时间
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description: 根据job和trigger删除任务
     */
    public void removeJob(JobKey jobKey, TriggerKey triggerKey) {
        try {
            LogUtils.info("RemoveJob: " + jobKey.getName() + "," + jobKey.getGroup());
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public static void startJobs(Scheduler schedule) {
        try {
            schedule.start();
        } catch (Exception e) {
            LogUtils.error(e);
            throw new RuntimeException(e);
        }
    }


    public void shutdownJobs(Scheduler schedule) {
        try {
            if (!schedule.isShutdown()) {
                schedule.shutdown();
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加或修改 cronJob
     */
    public void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron, JobDataMap jobDataMap)
            throws SchedulerException {
        LogUtils.info("AddOrUpdateCronJob: " + jobKey.getName() + "," + triggerKey.getGroup());

        if (scheduler.checkExists(triggerKey)) {
            modifyCronJobTime(triggerKey, cron);
        } else {
            addCronJob(jobKey, triggerKey, jobClass, cron, jobDataMap);
        }
    }

    public void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron) throws SchedulerException {
        addOrUpdateCronJob(jobKey, triggerKey, jobClass, cron, null);
    }

    public JobDataMap getDefaultJobDataMap(Schedule schedule, String expression, String userId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("resourceId", schedule.getResourceId());
        jobDataMap.put("expression", expression);
        jobDataMap.put("userId", userId);
        jobDataMap.put("config", schedule.getConfig());
        jobDataMap.put("projectId", schedule.getProjectId());
        return jobDataMap;
    }
}
