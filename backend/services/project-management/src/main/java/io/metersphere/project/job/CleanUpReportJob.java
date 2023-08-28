package io.metersphere.project.job;

import io.metersphere.sdk.sechedule.BaseScheduleJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class CleanUpReportJob extends BaseScheduleJob {



    @Override
    protected void businessExecute(JobExecutionContext context) {
        //TODO 定时任务执行 清除报告 invokerReportServices
        //serviceInvoker.invokeReportServices(projectId);
    }

    public static JobKey getJobKey(String projectId) {
        return new JobKey(projectId, CleanUpReportJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String projectId) {
        return new TriggerKey(projectId, CleanUpReportJob.class.getName());
    }
}
