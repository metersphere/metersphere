package io.metersphere.system.schedule;

import io.metersphere.sdk.util.LogUtils;
import org.quartz.*;

public abstract class BaseScheduleJob implements Job {

    protected String resourceId;

    protected String userId;

    protected String expression;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.resourceId = jobDataMap.getString("resourceId");
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");

        LogUtils.info(jobKey.getGroup() + " Running: " + resourceId);
        businessExecute(context);
    }

    protected abstract void businessExecute(JobExecutionContext context);
}
