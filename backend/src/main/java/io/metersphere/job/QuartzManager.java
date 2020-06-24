package io.metersphere.job;

import io.metersphere.commons.utils.LogUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzManager {

    public static StdSchedulerFactory sf = new StdSchedulerFactory();

    /**
     * 添加 simpleJob
     * @param jobKey
     * @param triggerKey
     * @param cls
     * @param repeatIntervalTime
     * @param jobDataMap
     * @throws SchedulerException
     */
    public static void addSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> cls, int repeatIntervalTime,
                              JobDataMap jobDataMap) throws SchedulerException {

        Scheduler sched = sf.getScheduler();

        JobDetail jd = JobBuilder.newJob(cls).withIdentity(jobKey).setJobData(jobDataMap).build();

        SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(repeatIntervalTime).repeatForever())
                .startNow().build();

        sched.scheduleJob(jd, trigger);

        try {

            if (!sched.isShutdown()) {
                sched.start();
            }

        } catch (SchedulerException e) {
            LogUtil.error(e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void addSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> cls, int repeatIntervalTime) throws SchedulerException {
       addSimpleJob(jobKey, triggerKey, cls, repeatIntervalTime);
    }

    /**
     * 添加 cronJob
     * @param jobKey
     * @param triggerKey
     * @param jobClass
     * @param cron
     * @param jobDataMap
     */
    public static void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron, JobDataMap jobDataMap) {
        try {
            JobBuilder jobBuilder = JobBuilder.newJob(jobClass).withIdentity(jobKey);
            if (jobDataMap != null) {
                jobBuilder.setJobData(jobDataMap);
            }
            JobDetail jobDetail = jobBuilder.build();

            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();

            triggerBuilder.withIdentity(triggerKey);

            triggerBuilder.startNow();

            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));

            CronTrigger trigger = (CronTrigger) triggerBuilder.build();

            Scheduler sched = sf.getScheduler();

            sched.scheduleJob(jobDetail, trigger);

            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron) {
        addCronJob(jobKey, triggerKey, jobClass, cron, null);
    }

    /**
     * 修改 cronTrigger
     * @param triggerKey
     * @param cron
     * @throws SchedulerException
     */
    public static void modifyCronJobTime(TriggerKey triggerKey, String cron) throws SchedulerException {

        Scheduler sched = sf.getScheduler();

        LogUtil.info("modifyCronJobTime: " + triggerKey.getName() + "," + triggerKey.getGroup());

        try {
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);

            if (trigger == null) {
                return;
            }

            String oldTime = trigger.getCronExpression();

            if (!oldTime.equalsIgnoreCase(cron)) {

                /** 方式一 ：调用 rescheduleJob 开始 */
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();// 触发器

                triggerBuilder.withIdentity(triggerKey);// 触发器名,触发器组

                triggerBuilder.startNow();// 立即执行

                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));// 触发器时间设定

                trigger = (CronTrigger) triggerBuilder.build();// 创建Trigger对象

                sched.rescheduleJob(triggerKey, trigger);// 修改一个任务的触发时间
                /** 方式一 ：调用 rescheduleJob 结束 */

                /** 方式二：先删除，然后在创建一个新的Job */
                // JobDetail jobDetail = sched.getJobDetail(JobKey.jobKey(jobName, jobGroupName));
                // Class<? extends Job> jobClass = jobDetail.getJobClass();
                // removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
                // addJob(jobName, jobGroupName, triggerName, triggerGroupName, jobClass, cron);
                /** 方式二 ：先删除，然后在创建一个新的Job */
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改simpleTrigger触发器的触发时间
     * @param triggerKey
     * @param repeatIntervalTime
     * @throws SchedulerException
     */
    public static void modifySimpleJobTime(TriggerKey triggerKey, int repeatIntervalTime) throws SchedulerException {

        Scheduler sched = sf.getScheduler();

        try {

            LogUtil.info("modifySimpleJobTime: " + triggerKey.getName() + "," + triggerKey.getGroup());

            SimpleTrigger trigger = (SimpleTrigger) sched.getTrigger(triggerKey);

            if (trigger == null) {
                return;
            }

            long oldTime = trigger.getRepeatInterval();

            if (oldTime != repeatIntervalTime) {

                /** 方式一 ：调用 rescheduleJob 开始 */
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();// 触发器builder

                triggerBuilder.withIdentity(triggerKey);// 触发器名,触发器组

                triggerBuilder.withSchedule(SimpleScheduleBuilder.repeatHourlyForever(repeatIntervalTime));// 更新触发器的重复间隔时间

                triggerBuilder.startNow();// 立即执行

                trigger = (SimpleTrigger) triggerBuilder.build();// 创建Trigger对象

                sched.rescheduleJob(triggerKey, trigger);// 修改一个任务的触发时间

                /** 方式一 ：调用 rescheduleJob 结束 */

                /** 方式二：先删除，然后在创建一个新的Job */
                // JobDetail jobDetail = sched.getJobDetail(JobKey.jobKey(jobName, jobGroupName));
                // Class<? extends Job> jobClass = jobDetail.getJobClass();
                // removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
                // addJob(jobName, jobGroupName, triggerName, triggerGroupName, jobClass, cron);
                /** 方式二 ：先删除，然后在创建一个新的Job */
            }

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @Title:
     * @Description: 根据job和trigger删除任务
     *
     * @param jobKey
     * @param triggerKey
     */
    public static void removeJob(JobKey jobKey, TriggerKey triggerKey) {

        try {

            LogUtil.info("RemoveJob: " + jobKey.getName() + "," + jobKey.getGroup());

            Scheduler sched = sf.getScheduler();

            sched.pauseTrigger(triggerKey);

            sched.unscheduleJob(triggerKey);

            sched.deleteJob(jobKey);

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public static void startJobs(Scheduler sched) {
        try {
            sched.start();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public static void shutdownJobs(Scheduler sched) {
        try {
            if (!sched.isShutdown()) {
                sched.shutdown();
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 新增或者修改 simpleJob
     * @param jobKey
     * @param triggerKey
     * @param clz
     * @param intervalTime
     * @param jobDataMap
     * @throws SchedulerException
     */
    public static void addOrUpdateSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class clz,
                                   int intervalTime, JobDataMap jobDataMap) throws SchedulerException {

        Scheduler sched = sf.getScheduler();

        if (sched.checkExists(triggerKey)) {
            modifySimpleJobTime(triggerKey, intervalTime);
        } else {
            addSimpleJob(jobKey, triggerKey, clz, intervalTime, jobDataMap);
        }

    }

    public static void addOrUpdateSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class clz, int intervalTime) throws SchedulerException {
        addOrUpdateSimpleJob(jobKey, triggerKey, clz, intervalTime, null);
    }


    /**
     * 添加或修改 cronJob
     * @param jobKey
     * @param triggerKey
     * @param jobClass
     * @param cron
     * @param jobDataMap
     * @throws SchedulerException
     */
    public static void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron, JobDataMap jobDataMap) throws SchedulerException {
        Scheduler sched = sf.getScheduler();

        LogUtil.info("AddOrUpdateCronJob: " + jobKey.getName() + "," + triggerKey.getGroup());

        if (sched.checkExists(triggerKey)) {
            modifyCronJobTime(triggerKey, cron);
        } else {
            addCronJob(jobKey, triggerKey, jobClass, cron, jobDataMap);
        }
    }

    public static void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron) throws SchedulerException {
        addOrUpdateCronJob(jobKey, triggerKey, jobClass, cron, null);
    }

    public static JobDataMap getDefaultJobDataMap(String resourceId, String expression) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("resourceId", resourceId);
        jobDataMap.put("expression", expression);
        return jobDataMap;
    }
}
