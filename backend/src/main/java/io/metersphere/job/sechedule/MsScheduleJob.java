package io.metersphere.job.sechedule;

import org.quartz.Job;

public abstract class MsScheduleJob implements Job{

    protected String resourceId;

    protected String expression;

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
