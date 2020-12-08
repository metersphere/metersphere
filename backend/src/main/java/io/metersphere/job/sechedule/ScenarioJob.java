package io.metersphere.job.sechedule;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.ArrayList;
import java.util.List;


public class ScenarioJob extends MsScheduleJob {

    ApiAutomationService apiAutomationService;

    public ScenarioJob() {
        apiAutomationService = (ApiAutomationService) CommonBeanFactory.getBean(ApiAutomationService.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        RunScenarioRequest request = new RunScenarioRequest();
        request.setId(resourceId);
        List<String> ids = new ArrayList<>();
        ids.add(resourceId);
        request.setScenarioIds(ids);
        request.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
        apiAutomationService.run(request);
    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.SCENARIO_TEST.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.SCENARIO_TEST.name());
    }
}
