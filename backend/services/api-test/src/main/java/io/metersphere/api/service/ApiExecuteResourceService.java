package io.metersphere.api.service;

import io.metersphere.api.invoker.ApiExecuteCallbackServiceInvoker;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ApiExecuteRunMode;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.mapper.ExecTaskItemMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiExecuteResourceService {

    @Resource
    private ExecTaskItemMapper execTaskItemMapper;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        String taskItemId = taskItem.getId();
        String reportId = taskItem.getReportId();
        LogUtils.info("生成并获取执行脚本: {}", taskItem.getId());

        ApiExecuteResourceType apiExecuteResourceType = EnumValidator.validateEnum(ApiExecuteResourceType.class, request.getResourceType());

        if (!ApiExecuteRunMode.isDebug(request.getRunMode())) {
            // 更新任务项状态
            ExecTaskItem execTaskItem = new ExecTaskItem();
            execTaskItem.setId(taskItem.getId());
            execTaskItem.setStartTime(System.currentTimeMillis());
            execTaskItem.setStatus(ExecStatus.RUNNING.name());
            execTaskItem.setThreadId(request.getThreadId());
            execTaskItemMapper.updateByPrimaryKeySelective(execTaskItem);

            // 非调试执行，更新报告状态
            switch (apiExecuteResourceType) {
                case API_SCENARIO, TEST_PLAN_API_SCENARIO, PLAN_RUN_API_SCENARIO ->
                        apiScenarioReportService.updateReportStatus(reportId, ExecStatus.RUNNING.name());
                case API_CASE, TEST_PLAN_API_CASE, PLAN_RUN_API_CASE ->
                        apiReportService.updateReportStatus(reportId, ExecStatus.RUNNING.name());
                default -> throw new MSException("不支持的资源类型: " + request.getResourceType());
            }
        }

        if (BooleanUtils.isFalse(request.getNeedParseScript())) {
            // 已经生成过脚本，直接获取
            String script = stringRedisTemplate.opsForValue().get(taskItemId);
            stringRedisTemplate.delete(taskItemId);
            GetRunScriptResult result = new GetRunScriptResult();
            result.setScript(Optional.ofNullable(script).orElse(StringUtils.EMPTY));
            return result;
        }

        return ApiExecuteCallbackServiceInvoker.getRunScript(request.getResourceType(), request);
    }
}