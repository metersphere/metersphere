package io.metersphere.job.sechedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.base.domain.SwaggerUrlProject;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString("resourceId");
        SwaggerUrlProject swaggerUrlProject = apiDefinitionService.getSwaggerInfo(resourceId);
        ApiTestImportRequest request = new ApiTestImportRequest();
        // 获取鉴权设置
        String config = swaggerUrlProject.getConfig();
        setAuthInfo(config, request);
        request.setProjectId(swaggerUrlProject.getProjectId());
        request.setSwaggerUrl(swaggerUrlProject.getSwaggerUrl());
        request.setModuleId(swaggerUrlProject.getModuleId());
        request.setPlatform("Swagger2");
        request.setUserId(jobDataMap.getString("userId"));
        request.setType("schedule");
        request.setUserId(jobDataMap.getString("userId"));
        request.setResourceId(resourceId);
        apiDefinitionService.apiTestImport(null, request);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, ScheduleGroup.SWAGGER_IMPORT.name());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, ScheduleGroup.SWAGGER_IMPORT.name());
    }

    public void setAuthInfo(String config, ApiTestImportRequest request){
        // 获取鉴权设置
        if(StringUtils.isNotBlank(config)){
            JSONObject configObj = JSON.parseObject(config);
            List<KeyValue> headers = JSONObject.parseArray(configObj.getString("headers"), KeyValue.class);
            if(CollectionUtils.isNotEmpty(headers)){
                request.setHeaders(headers);
            }
            List<KeyValue> arguments = JSONObject.parseArray(configObj.getString("arguments"), KeyValue.class);
            if(CollectionUtils.isNotEmpty(arguments)){
                request.setArguments(arguments);
            }
            MsAuthManager msAuthManager = JSONObject.parseObject(configObj.getString("authManager"), MsAuthManager.class);
            if(msAuthManager != null){
                request.setAuthManager(msAuthManager);
            }
        }
    }

}
