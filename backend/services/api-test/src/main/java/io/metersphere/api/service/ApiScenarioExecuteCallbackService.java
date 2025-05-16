package io.metersphere.api.service;


import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.invoker.ApiExecuteCallbackServiceInvoker;
import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.api.utils.ApiScenarioIntegratedReportStepCache;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ApiExecuteRunMode;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:47
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioExecuteCallbackService implements ApiExecuteCallbackService {
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private ApiScenarioBatchRunService apiScenarioBatchRunService;
    @Resource
    private ApiScenarioIntegratedReportStepCache apiScenarioIntegratedReportStepCache;

    public ApiScenarioExecuteCallbackService() {
        ApiExecuteCallbackServiceInvoker.register(ApiExecuteResourceType.API_SCENARIO, this);
    }

    /**
     * 解析并返回执行脚本
     */
    @Override
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        ApiScenarioDetail apiScenarioDetail = apiScenarioRunService.getForRunWithTaskItemErrorMassage(request.getTaskItem().getId(), request.getTaskItem().getResourceId());
        String reportId = initReport(request, apiScenarioDetail);
        GetRunScriptResult result = apiScenarioRunService.getRunScript(request, apiScenarioDetail);
        result.setReportId(reportId);
        return result;
    }

    private String initReport(GetRunScriptRequest request, ApiScenarioDetail apiScenarioDetail) {
        TaskItem taskItem = request.getTaskItem();
        String reportId = taskItem.getReportId();
        try {
            if (BooleanUtils.isTrue(request.getBatch())) {
                if (request.getRunModeConfig().isIntegratedReport()) {
                    // 避免接口重试，步骤重复生成
                    if (apiScenarioIntegratedReportStepCache.setIfAbsent(reportId, apiScenarioDetail.getId())) {
                        // 集合报告，生成一级步骤的子步骤
                        apiScenarioRunService.initScenarioReportSteps(apiScenarioDetail.getId(), apiScenarioDetail.getSteps(), reportId);
                    }
                } else {
                    // 批量执行，生成独立报告
                    reportId = apiScenarioBatchRunService.initScenarioReport(taskItem.getId(), reportId, request.getRunModeConfig(), apiScenarioDetail, request.getUserId());
                    // 初始化报告步骤
                    apiScenarioRunService.initScenarioReportSteps(apiScenarioDetail.getSteps(), reportId);
                }
            } else if (!ApiExecuteRunMode.isDebug(request.getRunMode())) {
                reportId = apiScenarioRunService.initApiScenarioReport(taskItem.getId(), apiScenarioDetail, request);
                // 初始化报告步骤
                apiScenarioRunService.initScenarioReportSteps(apiScenarioDetail.getSteps(), reportId);
            }
        } catch (DuplicateKeyException e) {
            if (!request.getRunModeConfig().isIntegratedReport()) {
                // 步骤中的 stepId 是执行时时随机生成的，如果重试，需要删除原有的步骤，重新生成，跟执行脚本匹配
                apiScenarioRunService.deleteStepsByReportId(reportId);
                apiScenarioRunService.initScenarioReportSteps(apiScenarioDetail.getSteps(), reportId);
            }
            // 避免重试，报告ID重复，导致执行失败
            LogUtils.error(e);
        }
        return reportId;
    }

    /**
     * 串行时，执行下一个任务
     *
     * @param queue
     * @param queueDetail
     */
    @Override
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        apiScenarioBatchRunService.executeNextTask(queue, queueDetail);
    }
}
