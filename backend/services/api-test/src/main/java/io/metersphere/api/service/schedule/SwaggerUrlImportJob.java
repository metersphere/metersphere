package io.metersphere.api.service.schedule;


import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.dto.definition.ApiScheduleDTO;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.service.definition.ApiDefinitionImportUtilService;
import io.metersphere.api.service.definition.ApiDefinitionScheduleService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.schedule.BaseScheduleJob;
import io.metersphere.system.service.NormalUserService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class SwaggerUrlImportJob extends BaseScheduleJob {
    private ApiDefinitionImportUtilService apiDefinitionImportUtilService;
    private ApiDefinitionScheduleService apiDefinitionScheduleService;
    private NormalUserService normalUserService;

    public SwaggerUrlImportJob() {
        apiDefinitionImportUtilService = CommonBeanFactory.getBean(ApiDefinitionImportUtilService.class);
        apiDefinitionScheduleService = CommonBeanFactory.getBean(ApiDefinitionScheduleService.class);
        normalUserService = CommonBeanFactory.getBean(NormalUserService.class);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString("resourceId");
        ApiScheduleDTO swaggerInfo = apiDefinitionScheduleService.getSchedule(resourceId);
        ImportRequest request = new ImportRequest();
        BeanUtils.copyBean(request, swaggerInfo);
        request.setPlatform(ApiImportPlatform.Swagger3.name());
        request.setUserId(jobDataMap.getString("userId"));
        request.setType("SCHEDULE");
        request.setResourceId(resourceId);
        apiDefinitionImportUtilService.apiTestImport(null, request, request.getProjectId());
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, SwaggerUrlImportJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, SwaggerUrlImportJob.class.getName());
    }

}
