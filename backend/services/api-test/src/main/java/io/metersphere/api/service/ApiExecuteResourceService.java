package io.metersphere.api.service;

import io.metersphere.api.invoker.ApiExecuteCallbackServiceInvoker;
import io.metersphere.api.utils.TaskRunningCache;
import io.metersphere.sdk.constants.ApiExecuteRunMode;
import io.metersphere.sdk.dto.api.task.ApiExecuteTaskResource;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
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
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TaskRunningCache taskRunningCache;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiExecuteTaskTokenService apiExecuteTaskTokenService;


    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        validateTaskItem(taskItem);
        LogUtils.info("生成并获取执行脚本: {}", taskItem.getId());

        if (!ApiExecuteRunMode.isDebug(request.getRunMode())) {
            updateRunningReportStatus(request);
        }

        if (BooleanUtils.isFalse(request.getNeedParseScript())) {
            // 已经生成过脚本，直接获取
            String scriptKey = apiExecuteTaskTokenService.getScriptKey(taskItem.getTaskToken());
            String script = stringRedisTemplate.opsForValue().get(scriptKey);
            stringRedisTemplate.delete(scriptKey);
            GetRunScriptResult result = new GetRunScriptResult();
            result.setScript(Optional.ofNullable(script).orElse(StringUtils.EMPTY));
            if (!ApiExecuteRunMode.isDebug(request.getRunMode())) {
                // 初始化报告
                String reportId = ApiExecuteCallbackServiceInvoker.initReport(request.getResourceType(), request);
                result.setReportId(reportId);
            }
            return result;
        }

        return ApiExecuteCallbackServiceInvoker.getRunScript(request.getResourceType(), request);
    }

    /**
     * 校验任务访问令牌
     * 接口为匿名访问，通过令牌校验任务项是否为服务端下发的任务
     */
    private void validateTaskItem(TaskItem taskItem) {
        if (taskItem == null) {
            throw new MSException(Translator.get("invalid_parameter"));
        }
        ApiExecuteTaskResource taskResource = apiExecuteTaskTokenService.getTaskResource(taskItem.getTaskToken());
        if (taskResource == null
                || !StringUtils.equals(taskResource.getTaskItemId(), taskItem.getId())
                || (StringUtils.isNotBlank(taskResource.getResourceId())
                && !StringUtils.equals(taskResource.getResourceId(), taskItem.getResourceId()))) {
            throw new MSException(Translator.get("invalid_parameter"));
        }
    }

    private void updateRunningReportStatus(GetRunScriptRequest request) {
        String taskId = request.getTaskId();

        // 重跑不更新任务状态
        if (BooleanUtils.isFalse(request.getRerun())) {
            if (request.getBatch()) {
                // 设置缓存成功说明是第一个任务，则设置任务的开始时间和运行状态
                if (taskRunningCache.setIfAbsent(taskId)) {
                    // 将任务状态更新为运行中
                    apiCommonService.updateTaskRunningStatus(taskId);
                }
            } else {
                // 非批量时，直接更新任务状态
                apiCommonService.updateTaskRunningStatus(taskId);
            }
        }

        // 更新任务项状态
        apiCommonService.updateTaskItemRunningStatus(request);
    }
}