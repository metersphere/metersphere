package io.metersphere.job.sechedule;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class SwaggerUrlImportJob extends MsScheduleJob {
    private ApiDefinitionService apiDefinitionService;

    public SwaggerUrlImportJob() {
        apiDefinitionService = (ApiDefinitionService) CommonBeanFactory.getBean(ApiDefinitionService.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString("resourceId");
        String swaggerUrl = jobDataMap.getString("swaggerUrl");
        ApiTestImportRequest request = new ApiTestImportRequest();
        request.setProjectId(resourceId);
        request.setSwaggerUrl(swaggerUrl);
        request.setPlatform("Swagger2");
        request.setSaved(true);
        apiDefinitionService.apiTestImport(null, request);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, ScheduleGroup.SWAGGER_IMPORT.name());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, ScheduleGroup.SWAGGER_IMPORT.name());
    }
}
