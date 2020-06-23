package io.metersphere.job.sechedule;

import io.metersphere.api.dto.SaveAPITestRequest;
import io.metersphere.api.service.APITestService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.job.QuartzManager;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;

public class ApiTestJob implements Job {

    private APITestService apiTestService;

    private String testId;

    private String cronExpression;

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public ApiTestJob() {
        apiTestService = (APITestService) CommonBeanFactory.getBean(APITestService.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (StringUtils.isBlank(testId)) {
            QuartzManager.removeJob(new JobKey(testId), new TriggerKey(testId));
        }
        LogUtil.info("ApiTestSchedule Running: " + testId);
        LogUtil.info("CronExpression: " + cronExpression);
        SaveAPITestRequest request = new SaveAPITestRequest();
        request.setId(testId);
        apiTestService.run(request);
    }
}
