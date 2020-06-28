package io.metersphere.job.sechedule;

import io.metersphere.api.dto.SaveAPITestRequest;
import io.metersphere.api.service.APITestService;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.job.QuartzManager;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;

public class ApiTestJob extends MsScheduleJob {

    private APITestService apiTestService;

    public ApiTestJob() {
        apiTestService = (APITestService) CommonBeanFactory.getBean(APITestService.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (StringUtils.isBlank(resourceId)) {
            QuartzManager.removeJob(getJobKey(resourceId), getTriggerKey(resourceId));
        }
        LogUtil.info("ApiTestSchedule Running: " + resourceId);
        LogUtil.info("CronExpression: " + expression);
        SaveAPITestRequest request = new SaveAPITestRequest();
        request.setId(resourceId);
        request.setUserId(userId);
        request.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
        apiTestService.run(request);
    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.API_TEST.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.API_TEST.name());
    }
}
