package io.metersphere.project.job;

import io.metersphere.system.sechedule.BaseScheduleJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class IssueSyncJob extends BaseScheduleJob {
    @Override
    protected void businessExecute(JobExecutionContext context) {
        //TODO 定时任务执行 同步issue
    }

    public static JobKey getJobKey(String projectId) {
        return new JobKey(projectId, IssueSyncJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String projectId) {
        return new TriggerKey(projectId, IssueSyncJob.class.getName());
    }
}
