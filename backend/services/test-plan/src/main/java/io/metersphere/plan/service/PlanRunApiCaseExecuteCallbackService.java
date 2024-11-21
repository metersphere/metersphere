package io.metersphere.plan.service;

import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.invoker.ApiExecuteCallbackServiceInvoker;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteCallbackService;
import io.metersphere.api.service.definition.ApiTestCaseRunService;
import io.metersphere.plan.domain.TestPlanReportApiCase;
import io.metersphere.plan.mapper.TestPlanReportApiCaseMapper;
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
public class PlanRunApiCaseExecuteCallbackService implements ApiExecuteCallbackService {
    @Resource
    private TestPlanExecuteService testPlanExecuteService;

    @Resource
    private PlanRunTestPlanApiCaseService planRunTestPlanApiCaseService;

    @Resource
    private TestPlanReportApiCaseMapper testPlanReportApiCaseMapper;

    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;

    @Resource
    private ApiTestCaseRunService apiTestCaseRunService;

    @Resource
    private ApiCommonService apiCommonService;

    public PlanRunApiCaseExecuteCallbackService() {
        ApiExecuteCallbackServiceInvoker.register(ApiExecuteResourceType.PLAN_RUN_API_CASE, this);
    }

    /**
     * 解析并返回执行脚本
     */
    @Override
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TestPlanReportApiCase testPlanReportApiCase = testPlanReportApiCaseMapper.selectByPrimaryKey(request.getTaskItem().getResourceId());
        ApiTestCase apiTestCase = testPlanReportApiCase == null ? null : apiTestCaseMapper.selectByPrimaryKey(testPlanReportApiCase.getApiCaseId());
        if (testPlanReportApiCase == null || apiTestCase == null || apiTestCase.getDeleted()) {
            apiCommonService.updateTaskItemErrorMassage(request.getTaskItem().getId(), TaskItemErrorMessage.CASE_NOT_EXIST);
            throw new MSException(ApiResultCode.CASE_NOT_EXIST);
        }
        String reportId = initReport(request, testPlanReportApiCase, apiTestCase);
        GetRunScriptResult result = apiTestCaseRunService.getRunScript(request, apiTestCase);
        result.setReportId(reportId);
        return result;
    }

    public String initReport(GetRunScriptRequest request, TestPlanReportApiCase testPlanReportApiCase, ApiTestCase apiTestCase) {
        try {
            return planRunTestPlanApiCaseService.initApiReport(request, testPlanReportApiCase, apiTestCase);
        } catch (DuplicateKeyException e) {
            // 避免重试，报告ID重复，导致执行失败
            LogUtils.error(e);
        }
        return request.getTaskItem().getReportId();
    }

    /**
     * 串行时，执行下一个任务
     * @param queue
     * @param queueDetail
     */
    @Override
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        planRunTestPlanApiCaseService.executeNextTask(queue, queueDetail);
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
