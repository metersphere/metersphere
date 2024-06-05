package io.metersphere.plan.job;

import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.service.TestPlanExecuteService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.schedule.BaseScheduleJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.Collections;

public class TestPlanScheduleJob extends BaseScheduleJob {

    @Override
    protected void businessExecute(JobExecutionContext context) {
        TestPlanExecuteService testPlanExecuteService = CommonBeanFactory.getBean(TestPlanExecuteService.class);
        assert testPlanExecuteService != null;
        testPlanExecuteService.execute(new TestPlanExecuteRequest() {{
            this.setExecuteIds(Collections.singletonList(resourceId));
        }}, userId);
    }


    public static JobKey getJobKey(String testPlanId) {
        return new JobKey(testPlanId, TestPlanScheduleJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String testPlanId) {
        return new TriggerKey(testPlanId, TestPlanScheduleJob.class.getName());
    }
}
