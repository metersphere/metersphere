package io.metersphere.job.sechedule;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.base.domain.SwaggerUrlProject;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class SwaggerUrlImportJob extends MsScheduleJob {
    private ApiDefinitionService apiDefinitionService;

    public SwaggerUrlImportJob() {
         apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString("resourceId");
        SwaggerUrlProject swaggerUrlProject=apiDefinitionService.getSwaggerInfo(resourceId);
        ApiTestImportRequest request = new ApiTestImportRequest();
        request.setProjectId(swaggerUrlProject.getProjectId());
        request.setSwaggerUrl(swaggerUrlProject.getSwaggerUrl());
        request.setModuleId(swaggerUrlProject.getModuleId());
        request.setPlatform("Swagger2");
        request.setUserId(jobDataMap.getString("userId"));
        request.setType("schedule");
        request.setUserId(jobDataMap.getString("userId"));
        apiDefinitionService.apiTestImport(null, request);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, ScheduleGroup.SWAGGER_IMPORT.name());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, ScheduleGroup.SWAGGER_IMPORT.name());
    }
}
