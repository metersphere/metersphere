package io.metersphere.job.sechedule;

import io.metersphere.commons.utils.LogUtil;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ScheduleManager {

    @Resource
    private Scheduler scheduler;

    /**
     * 添加 simpleJob
     *
     * @param jobKey
     * @param triggerKey
     * @param cls
     * @param repeatIntervalTime
     * @param jobDataMap
     * @throws SchedulerException
     */
    public void addSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> cls, int repeatIntervalTime,
                             JobDataMap jobDataMap) throws SchedulerException {

        JobBuilder jobBuilder = JobBuilder.newJob(cls).withIdentity(jobKey);

        if (jobDataMap != null) {
            jobBuilder.usingJobData(jobDataMap);
        }

        JobDetail jd = jobBuilder.build();

        SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(repeatIntervalTime).repeatForever())
                .startNow().build();

        scheduler.scheduleJob(jd, trigger);
    }

    public void addSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> cls, int repeatIntervalTime) throws SchedulerException {
        addSimpleJob(jobKey, triggerKey, cls, repeatIntervalTime);
    }

    /**
     * 添加 cronJob
     *
     * @param jobKey
     * @param triggerKey
     * @param jobClass
     * @param cron
     * @param jobDataMap
     */
    public void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron, JobDataMap jobDataMap) {
        try {

            LogUtil.info("addCronJob: " + triggerKey.getName() + "," + triggerKey.getGroup());

            JobBuilder jobBuilder = JobBuilder.newJob(jobClass).withIdentity(jobKey);
            if (jobDataMap != null) {
                jobBuilder.usingJobData(jobDataMap);
            }
            JobDetail jobDetail = jobBuilder.build();

            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();

            triggerBuilder.withIdentity(triggerKey);

            triggerBuilder.startNow();

            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));

            CronTrigger trigger = (CronTrigger) triggerBuilder.build();

            scheduler.scheduleJob(jobDetail, trigger);

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron) {
        addCronJob(jobKey, triggerKey, jobClass, cron, null);
    }

    /**
     * 修改 cronTrigger
     *
     * @param triggerKey
     * @param cron
     * @throws SchedulerException
     */
    public void modifyCronJobTime(TriggerKey triggerKey, String cron) throws SchedulerException {

        LogUtil.info("modifyCronJobTime: " + triggerKey.getName() + "," + triggerKey.getGroup());

        try {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

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

                scheduler.rescheduleJob(triggerKey, trigger);// 修改一个任务的触发时间
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
     *
     * @param triggerKey
     * @param repeatIntervalTime
     * @throws SchedulerException
     */
    public void modifySimpleJobTime(TriggerKey triggerKey, int repeatIntervalTime) throws SchedulerException {

        try {

            LogUtil.info("modifySimpleJobTime: " + triggerKey.getName() + "," + triggerKey.getGroup());

            SimpleTrigger trigger = (SimpleTrigger) scheduler.getTrigger(triggerKey);

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

                scheduler.rescheduleJob(triggerKey, trigger);// 修改一个任务的触发时间

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
     * @param jobKey
     * @param triggerKey
     * @Title:
     * @Description: 根据job和trigger删除任务
     */
    public void removeJob(JobKey jobKey, TriggerKey triggerKey) {

        try {

            LogUtil.info("RemoveJob: " + jobKey.getName() + "," + jobKey.getGroup());

            scheduler.pauseTrigger(triggerKey);

            scheduler.unscheduleJob(triggerKey);

            scheduler.deleteJob(jobKey);

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


    public void shutdownJobs(Scheduler sched) {
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
     *
     * @param jobKey
     * @param triggerKey
     * @param clz
     * @param intervalTime
     * @param jobDataMap
     * @throws SchedulerException
     */
    public void addOrUpdateSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class clz,
                                     int intervalTime, JobDataMap jobDataMap) throws SchedulerException {

        if (scheduler.checkExists(triggerKey)) {
            modifySimpleJobTime(triggerKey, intervalTime);
        } else {
            addSimpleJob(jobKey, triggerKey, clz, intervalTime, jobDataMap);
        }

    }

    public void addOrUpdateSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class clz, int intervalTime) throws SchedulerException {
        addOrUpdateSimpleJob(jobKey, triggerKey, clz, intervalTime, null);
    }


    /**
     * 添加或修改 cronJob
     *
     * @param jobKey
     * @param triggerKey
     * @param jobClass
     * @param cron
     * @param jobDataMap
     * @throws SchedulerException
     */
    public void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron, JobDataMap jobDataMap) throws SchedulerException {

        LogUtil.info("AddOrUpdateCronJob: " + jobKey.getName() + "," + triggerKey.getGroup());

        if (scheduler.checkExists(triggerKey)) {
            modifyCronJobTime(triggerKey, cron);
        } else {
            addCronJob(jobKey, triggerKey, jobClass, cron, jobDataMap);
        }
    }

    public void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron) throws SchedulerException {
        addOrUpdateCronJob(jobKey, triggerKey, jobClass, cron, null);
    }

    public JobDataMap getDefaultJobDataMap(String resourceId, String expression, String userId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("resourceId", resourceId);
        jobDataMap.put("expression", expression);
        jobDataMap.put("userId", userId);
        return jobDataMap;
    }
}
