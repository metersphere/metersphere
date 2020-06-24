package io.metersphere.job.sechedule;

import org.quartz.Job;

public abstract class MsScheduleJob implements Job{

    protected String resourceId;

    protected String userId;

    protected String expression;

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
