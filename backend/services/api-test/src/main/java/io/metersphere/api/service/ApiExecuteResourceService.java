package io.metersphere.api.service;

import io.metersphere.api.invoker.ApiExecuteCallbackServiceInvoker;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiExecuteResourceService {

    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        String reportId = taskItem.getReportId();
        String key = apiExecuteService.getTaskKey(reportId, taskItem.getResourceId());
        LogUtils.info("生成并获取执行脚本: {}", key);

        ApiExecuteResourceType apiExecuteResourceType = EnumValidator.validateEnum(ApiExecuteResourceType.class, request.getResourceType());

        switch (apiExecuteResourceType) {
            case API_SCENARIO, TEST_PLAN_API_SCENARIO, PLAN_RUN_API_SCENARIO ->
                    apiScenarioReportService.updateReportStatus(reportId, ExecStatus.RUNNING.name());
            case API_CASE, TEST_PLAN_API_CASE, PLAN_RUN_API_CASE ->
                    apiReportService.updateReportStatus(reportId, ExecStatus.RUNNING.name());
            default -> throw new MSException("不支持的资源类型: " + request.getResourceType());
        }

        return ApiExecuteCallbackServiceInvoker.getRunScript(request.getResourceType(), request);
    }

    public String getRunScript(String reportId, String testId) {
        String key = apiExecuteService.getTaskKey(reportId, testId);
        LogUtils.info("获取执行脚本: {}", key);
        String script = stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.delete(key);
        apiReportService.updateReportStatus(reportId, ExecStatus.RUNNING.name());
        apiScenarioReportService.updateReportStatus(reportId, ExecStatus.RUNNING.name());
        return Optional.ofNullable(script).orElse(StringUtils.EMPTY);
    }
}