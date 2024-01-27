package io.metersphere.api.service.schedule;


import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.dto.definition.ApiScheduleDTO;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.service.definition.ApiDefinitionScheduleService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.schedule.BaseScheduleJob;
import io.metersphere.system.service.UserService;
import io.metersphere.system.utils.SessionUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class SwaggerUrlImportJob extends BaseScheduleJob {
    private ApiDefinitionService apiDefinitionService;
    private ApiDefinitionScheduleService apiDefinitionScheduleService;
    private UserService userService;

    public SwaggerUrlImportJob() {
        apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
        apiDefinitionScheduleService = CommonBeanFactory.getBean(ApiDefinitionScheduleService.class);
        userService = CommonBeanFactory.getBean(UserService.class);
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

        UserDTO userDTO = userService.getUserDTOByKeyword(request.getUserId());
        SessionUser user = SessionUser.fromUser(userDTO, SessionUtils.getSessionId());
        apiDefinitionService.apiTestImport(null, request, user, request.getProjectId());
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, SwaggerUrlImportJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, SwaggerUrlImportJob.class.getName());
    }

}
