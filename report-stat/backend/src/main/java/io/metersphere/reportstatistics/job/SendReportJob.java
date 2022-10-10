package io.metersphere.reportstatistics.job;

import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.reportstatistics.service.EnterpriseTestReportService;
import org.quartz.*;

public class SendReportJob implements Job {
    protected String resourceId;
    protected String userId;
    protected String expression;

    private EnterpriseTestReportService enterpriseTestReportService;

    public SendReportJob() {
        this.enterpriseTestReportService = CommonBeanFactory.getBean(EnterpriseTestReportService.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.resourceId = jobDataMap.getString("resourceId");
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");
        businessExecute(context);
    }

    void businessExecute(JobExecutionContext context) {
        try {
            enterpriseTestReportService.sendEmail(resourceId, true);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }

    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.SCHEDULE_SEND_REPORT.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.SCHEDULE_SEND_REPORT.name());
    }
}
