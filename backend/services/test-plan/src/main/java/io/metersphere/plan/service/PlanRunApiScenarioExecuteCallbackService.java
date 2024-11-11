package io.metersphere.plan.service;

import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.invoker.ApiExecuteCallbackServiceInvoker;
import io.metersphere.api.service.ApiExecuteCallbackService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.plan.domain.TestPlanReportApiScenario;
import io.metersphere.plan.mapper.TestPlanReportApiScenarioMapper;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import jakarta.annotation.Resource;
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
        ApiScenarioDetail apiScenarioDetail = apiScenarioRunService.getForRun(testPlanReportApiScenario.getApiScenarioId());
        apiScenarioDetail.setEnvironmentId(testPlanReportApiScenario.getEnvironmentId());
        apiScenarioDetail.setGrouped(testPlanReportApiScenario.getGrouped());
        GetRunScriptResult result = planRunTestPlanApiScenarioService.getRunScript(request);
        String reportId = initReport(request, testPlanReportApiScenario, apiScenarioDetail);
        result.setReportId(reportId);
        return result;
    }

    @Override
    public String initReport(GetRunScriptRequest request) {
        String resourceId = request.getTaskItem().getResourceId();
        TestPlanReportApiScenario testPlanReportApiScenario = testPlanReportApiScenarioMapper.selectByPrimaryKey(resourceId);
        ApiScenarioDetail apiScenarioDetail = apiScenarioRunService.getForRun(testPlanReportApiScenario.getApiScenarioId());
        return initReport(request, testPlanReportApiScenario, apiScenarioDetail);
    }

    public String initReport(GetRunScriptRequest request, TestPlanReportApiScenario testPlanReportApiScenario, ApiScenarioDetail apiScenarioDetail) {
        String reportId = planRunTestPlanApiScenarioService.initReport(request, testPlanReportApiScenario, apiScenarioDetail);
        // 初始化报告步骤
        apiScenarioRunService.initScenarioReportSteps(apiScenarioDetail.getSteps(), reportId);
        return reportId;
    }

    /**
     * 串行时，执行下一个任务
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
     * @param apiNoticeDTO
     */
    @Override
    public void executeNextCollection(ApiNoticeDTO apiNoticeDTO, boolean isStopOnFailure) {
        testPlanExecuteService.collectionExecuteQueueFinish(apiNoticeDTO.getParentQueueId(), isStopOnFailure);
    }
}
