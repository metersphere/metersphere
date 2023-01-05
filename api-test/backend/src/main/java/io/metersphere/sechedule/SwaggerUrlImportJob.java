package io.metersphere.sechedule;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.base.domain.SwaggerUrlProject;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.service.definition.ApiDefinitionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.List;

public class SwaggerUrlImportJob extends MsScheduleJob {
    private ApiDefinitionService apiDefinitionService;

    public SwaggerUrlImportJob() {
        apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString(ElementConstants.RESOURCE_ID);
        SwaggerUrlProject swaggerUrlProject = apiDefinitionService.getSwaggerInfo(resourceId);
        ApiTestImportRequest request = new ApiTestImportRequest();
        // 获取鉴权设置
        String config = swaggerUrlProject.getConfig();
        setAuthInfo(config, request);
        request.setProjectId(swaggerUrlProject.getProjectId());
        request.setSwaggerUrl(swaggerUrlProject.getSwaggerUrl());
        request.setModuleId(swaggerUrlProject.getModuleId());
        request.setModeId(swaggerUrlProject.getModeId());
        request.setCoverModule(swaggerUrlProject.getCoverModule());
        request.setPlatform("Swagger2");
        request.setUserId(jobDataMap.getString("userId"));
        request.setType("schedule");
        request.setResourceId(resourceId);
        apiDefinitionService.apiTestImport(null, request);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, ScheduleGroup.SWAGGER_IMPORT.name());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, ScheduleGroup.SWAGGER_IMPORT.name());
    }

    public void setAuthInfo(String config, ApiTestImportRequest request) {
        // 获取鉴权设置
        if (StringUtils.isNotBlank(config)) {
            JSONObject configObj = JSONUtil.parseObject(config);
            List<KeyValue> headers = JSONUtil.parseArray(configObj.optString("headers"), KeyValue.class);
            if (CollectionUtils.isNotEmpty(headers)) {
                request.setHeaders(headers);
            }
            List<KeyValue> arguments = JSONUtil.parseArray(configObj.optString("arguments"), KeyValue.class);
            if (CollectionUtils.isNotEmpty(arguments)) {
                request.setArguments(arguments);
            }
            if (StringUtils.isNotBlank(configObj.optString("authManager"))) {
                MsAuthManager msAuthManager = JSONUtil.parseObject(configObj.optString("authManager"), MsAuthManager.class);
                if (msAuthManager != null) {
                    request.setAuthManager(msAuthManager);
                }
            }
        }
    }

}
