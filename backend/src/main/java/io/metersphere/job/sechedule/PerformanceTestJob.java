package io.metersphere.job.sechedule;

import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.job.QuartzManager;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.request.testplan.RunTestPlanRequest;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class PerformanceTestJob extends MsScheduleJob {

    private PerformanceTestService performanceTestService;

    public PerformanceTestJob() {
        this.performanceTestService = CommonBeanFactory.getBean(PerformanceTestService.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (StringUtils.isBlank(resourceId)) {
            QuartzManager.removeJob(getJobKey(resourceId), getTriggerKey(resourceId));
        }
        LogUtil.info("PerformanceTestSchedule Running: " + resourceId);
        LogUtil.info("CronExpression: " + expression);
        RunTestPlanRequest request = new RunTestPlanRequest();
        request.setId(resourceId);
        request.setUserId(userId);
        request.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
        performanceTestService.run(request);

    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.PERFORMANCE_TEST.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.PERFORMANCE_TEST.name());
    }

}
