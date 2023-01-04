package io.metersphere.commons.utils;


import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.plan.TestPlanApiCaseInfoDTO;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.enums.StorageEnums;
import io.metersphere.dto.RunModeConfigDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.UUID;

public class ApiDefinitionExecResultUtil {
    public static ApiDefinitionExecResultWithBLOBs initBase(String resourceId, String status, String reportId, RunModeConfigDTO config) {
        ApiDefinitionExecResultWithBLOBs apiResult = new ApiDefinitionExecResultWithBLOBs();
        if (StringUtils.isEmpty(reportId)) {
            apiResult.setId(UUID.randomUUID().toString());
        } else {
            apiResult.setId(reportId);
        }
        apiResult.setCreateTime(System.currentTimeMillis());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setEndTime(System.currentTimeMillis());
        apiResult.setTriggerMode(TriggerMode.BATCH.name());
        apiResult.setActuator(StorageEnums.LOCAL.name());
        if (config != null && GenerateHashTreeUtil.isResourcePool(config.getResourcePoolId()).isPool()) {
            apiResult.setActuator(config.getResourcePoolId());
        }
        apiResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiResult.setResourceId(resourceId);
        apiResult.setReportType(ReportTypeConstants.API_INDEPENDENT.name());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setType(ApiRunMode.DEFINITION.name());
        apiResult.setStatus(status);
        apiResult.setEnvConfig(JSON.toJSONString(config));

        return apiResult;
    }

    public static ApiDefinitionExecResultWithBLOBs addResult(
            BatchRunDefinitionRequest request,
            RunModeConfigDTO runModeConfigDTO,
            TestPlanApiCaseInfoDTO key,
            String status,
            ApiTestCase testCase,
            String poolId) {

        ApiDefinitionExecResultWithBLOBs apiResult = new ApiDefinitionExecResultWithBLOBs();
        apiResult.setId(UUID.randomUUID().toString());
        apiResult.setCreateTime(System.currentTimeMillis());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setEndTime(System.currentTimeMillis());
        apiResult.setReportType(ReportTypeConstants.API_INDEPENDENT.name());
        if (testCase != null) {
            apiResult.setName(testCase.getName());
            apiResult.setProjectId(testCase.getProjectId());
            apiResult.setVersionId(testCase.getVersionId());
        }
        apiResult.setTriggerMode(request.getTriggerMode());
        apiResult.setActuator(StorageEnums.LOCAL.name());
        if (StringUtils.isNotEmpty(poolId)) {
            apiResult.setActuator(poolId);
        }
        apiResult.setUserId(request.getUserId());
        if (StringUtils.isEmpty(apiResult.getUserId())) {
            apiResult.setUserId(SessionUtils.getUserId());
        }

        apiResult.setResourceId(key.getId());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setType(ApiRunMode.API_PLAN.name());
        apiResult.setStatus(status);
        apiResult.setContent(request.getPlanReportId());
        apiResult.setRelevanceTestPlanReportId(request.getPlanReportId());
        apiResult.setEnvConfig(JSON.toJSONString(runModeConfigDTO));

        return apiResult;
    }

    public static ApiDefinitionExecResultWithBLOBs add(String resourceId, String status, String reportId, String userId) {
        ApiDefinitionExecResultWithBLOBs apiResult = new ApiDefinitionExecResultWithBLOBs();
        if (StringUtils.isEmpty(reportId)) {
            apiResult.setId(UUID.randomUUID().toString());
        } else {
            apiResult.setId(reportId);
        }
        apiResult.setReportType(ReportTypeConstants.API_INDEPENDENT.name());
        apiResult.setCreateTime(System.currentTimeMillis());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setEndTime(System.currentTimeMillis());
        apiResult.setTriggerMode(TriggerMode.BATCH.name());
        apiResult.setActuator(StorageEnums.LOCAL.name());
        apiResult.setUserId(userId);
        apiResult.setResourceId(resourceId);
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setType(ApiRunMode.DEFINITION.name());
        apiResult.setStatus(status);
        return apiResult;
    }
}
