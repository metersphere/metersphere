package io.metersphere.api.exec.utils;

import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.RunModeConfigDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.UUID;

public class ApiDefinitionExecResultUtil {
    public static ApiDefinitionExecResult initBase(String resourceId, String status, String reportId, RunModeConfigDTO config) {
        ApiDefinitionExecResult apiResult = new ApiDefinitionExecResult();
        if (StringUtils.isEmpty(reportId)) {
            apiResult.setId(UUID.randomUUID().toString());
        } else {
            apiResult.setId(reportId);
        }
        apiResult.setCreateTime(System.currentTimeMillis());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setEndTime(System.currentTimeMillis());
        apiResult.setTriggerMode(TriggerMode.BATCH.name());
        apiResult.setActuator("LOCAL");
        if (config != null && GenerateHashTreeUtil.isResourcePool(config.getResourcePoolId()).isPool()) {
            apiResult.setActuator(config.getResourcePoolId());
        }
        apiResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiResult.setResourceId(resourceId);
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setType(ApiRunMode.DEFINITION.name());
        apiResult.setStatus(status);
        return apiResult;
    }

    public static ApiDefinitionExecResult addResult(BatchRunDefinitionRequest request, TestPlanApiCase key, String status, ApiDefinitionExecResultMapper batchMapper) {
        ApiDefinitionExecResult apiResult = new ApiDefinitionExecResult();
        apiResult.setId(UUID.randomUUID().toString());
        apiResult.setCreateTime(System.currentTimeMillis());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setEndTime(System.currentTimeMillis());
        ApiTestCaseWithBLOBs caseWithBLOBs = CommonBeanFactory.getBean(ApiTestCaseMapper.class).selectByPrimaryKey(key.getApiCaseId());
        if (caseWithBLOBs != null) {
            apiResult.setName(caseWithBLOBs.getName());
        }
        apiResult.setTriggerMode(request.getTriggerMode());
        apiResult.setActuator("LOCAL");
        if (request.getConfig() != null && GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()).isPool()) {
            apiResult.setActuator(request.getConfig().getResourcePoolId());
        }
        if (StringUtils.isEmpty(request.getUserId())) {
            if (SessionUtils.getUser() != null) {
                apiResult.setUserId(SessionUtils.getUser().getId());
            }
        } else {
            apiResult.setUserId(request.getUserId());
        }

        apiResult.setResourceId(key.getApiCaseId());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setType(ApiRunMode.API_PLAN.name());
        apiResult.setStatus(status);
        apiResult.setContent(request.getPlanReportId());
        batchMapper.insert(apiResult);
        return apiResult;
    }

    public static ApiDefinitionExecResult add(String resourceId, String status, String reportId) {
        ApiDefinitionExecResult apiResult = new ApiDefinitionExecResult();
        if (StringUtils.isEmpty(reportId)) {
            apiResult.setId(UUID.randomUUID().toString());
        } else {
            apiResult.setId(reportId);
        }
        apiResult.setCreateTime(System.currentTimeMillis());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setEndTime(System.currentTimeMillis());
        apiResult.setTriggerMode(TriggerMode.BATCH.name());
        apiResult.setActuator("LOCAL");
        apiResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiResult.setResourceId(resourceId);
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setType(ApiRunMode.DEFINITION.name());
        apiResult.setStatus(status);
        return apiResult;
    }
}
