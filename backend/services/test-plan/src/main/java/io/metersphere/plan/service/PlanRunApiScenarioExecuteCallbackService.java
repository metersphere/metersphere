package io.metersphere.plan.service;

import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.invoker.ApiExecuteCallbackServiceInvoker;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteCallbackService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.plan.domain.TestPlanReportApiScenario;
import io.metersphere.plan.mapper.TestPlanReportApiScenarioMapper;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.TaskItemErrorMessage;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:47
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PlanRunApiScenarioExecuteCallbackService implements ApiExecuteCallbackService {
    @Resource
    private TestPlanExecuteService testPlanExecuteService;
    @Resource
    private PlanRunTestPlanApiScenarioService planRunTestPlanApiScenarioService;
    @Resource
    private TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private ApiCommonService apiCommonService;

    public PlanRunApiScenarioExecuteCallbackService() {
        ApiExecuteCallbackServiceInvoker.register(ApiExecuteResourceType.PLAN_RUN_API_SCENARIO, this);
    }

    /**
     * 解析并返回执行脚本
     */
    @Override
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        String resourceId = request.getTaskItem().getResourceId();
        TestPlanReportApiScenario testPlanReportApiScenario = testPlanReportApiScenarioMapper.selectByPrimaryKey(resourceId);
        if (testPlanReportApiScenario == null) {
            apiCommonService.updateTaskItemErrorMassage(request.getTaskItem().getId(), TaskItemErrorMessage.CASE_NOT_EXIST);
            throw new MSException(ApiResultCode.CASE_NOT_EXIST);
        }
        ApiScenarioDetail apiScenarioDetail = apiScenarioRunService.getForRunWithTaskItemErrorMassage(request.getTaskItem().getId(), testPlanReportApiScenario.getApiScenarioId());
        apiScenarioDetail.setEnvironmentId(testPlanReportApiScenario.getEnvironmentId());
        apiScenarioDetail.setGrouped(testPlanReportApiScenario.getGrouped());
        GetRunScriptResult result = apiScenarioRunService.getRunScript(request, apiScenarioDetail);
        String reportId = initReport(request, testPlanReportApiScenario, apiScenarioDetail);
        result.setReportId(reportId);
        return result;
    }

    public String initReport(GetRunScriptRequest request, TestPlanReportApiScenario testPlanReportApiScenario, ApiScenarioDetail apiScenarioDetail) {
        String reportId = request.getTaskItem().getReportId();
        try {
            planRunTestPlanApiScenarioService.initReport(request, testPlanReportApiScenario, apiScenarioDetail);
            // 初始化报告步骤
            apiScenarioRunService.initScenarioReportSteps(apiScenarioDetail.getSteps(), reportId);
        } catch (DuplicateKeyException e) {
            // 避免重试，报告ID重复，导致执行失败
            // 步骤中的 stepId 是执行时时随机生成的，如果重试，需要删除原有的步骤，重新生成，跟执行脚本匹配
            apiScenarioRunService.deleteStepsByReportId(reportId);
            apiScenarioRunService.initScenarioReportSteps(apiScenarioDetail.getSteps(), reportId);
            LogUtils.error(e);
        }
        return request.getTaskItem().getReportId();
    }

    /**
     * 串行时，执行下一个任务
     *
     * @param queue
     * @param queueDetail
     */
    @Override
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        planRunTestPlanApiScenarioService.executeNextTask(queue, queueDetail);
    }

    /**
     * 批量串行的测试集执行时
     * 测试集下用例执行完成时回调
     *
     * @param apiNoticeDTO
     */
    @Override
    public void executeNextCollection(ApiNoticeDTO apiNoticeDTO, boolean isStopOnFailure) {
        testPlanExecuteService.collectionExecuteQueueFinish(apiNoticeDTO.getParentQueueId(), isStopOnFailure);
    }
}
