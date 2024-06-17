package io.metersphere.plan.service;

import io.metersphere.api.invoker.ApiExecuteCallbackServiceInvoker;
import io.metersphere.api.service.ApiExecuteCallbackService;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
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
public class TestPlanApiCaseExecuteCallbackService implements ApiExecuteCallbackService {
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanApiCaseBatchRunService testPlanApiCaseBatchRunService;

    public TestPlanApiCaseExecuteCallbackService() {
        ApiExecuteCallbackServiceInvoker.register(ApiExecuteResourceType.TEST_PLAN_API_CASE, this);
    }

    /**
     * 解析并返回执行脚本
     */
    @Override
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        return testPlanApiCaseService.getRunScript(request);
    }

    /**
     * 串行时，执行下一个任务
     * @param queue
     * @param queueDetail
     */
    @Override
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        testPlanApiCaseBatchRunService.executeNextTask(queue, queueDetail);
    }

    /**
     * 批量串行的测试集执行时
     * 测试集下用例执行完成时回调
     * @param parentQueueId
     */
    @Override
    public void executeNextCollection(String parentQueueId) {
        testPlanApiCaseBatchRunService.executeNextCollection(parentQueueId);
    }

    /**
     * 失败停止时，删除测试集合队列
     * @param parentQueueId
     */
    @Override
    public void stopCollectionOnFailure(String parentQueueId) {
        testPlanApiCaseBatchRunService.stopCollectionOnFailure(parentQueueId);
    }
}
