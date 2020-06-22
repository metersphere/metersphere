package io.metersphere.schedule.job;

import io.metersphere.api.service.APITestService;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ApiTestJob implements Job {

    private APITestService apiTestService;

    public ApiTestJob() {
        apiTestService = (APITestService) CommonBeanFactory.getBean("apiTestService");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }
}
