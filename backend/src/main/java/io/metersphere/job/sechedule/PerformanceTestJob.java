package io.metersphere.job.sechedule;

import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.performance.service.PerformanceTestService;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class PerformanceTestJob extends MsScheduleJob {

    private PerformanceTestService performanceTestService;

    public PerformanceTestJob() {
        this.performanceTestService = CommonBeanFactory.getBean(PerformanceTestService.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
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
