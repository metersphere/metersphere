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
public class PlanRunApiScenarioExecuteCallbackService implements ApiExecuteCallbackService {
    @Resource
    private TestPlanExecuteService testPlanExecuteService;

    @Resource
    private PlanRunTestPlanApiScenarioService planRunTestPlanApiScenarioService;

    public PlanRunApiScenarioExecuteCallbackService() {
        ApiExecuteCallbackServiceInvoker.register(ApiExecuteResourceType.PLAN_RUN_API_SCENARIO, this);
    }

    /**
     * 解析并返回执行脚本
     */
    @Override
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        return planRunTestPlanApiScenarioService.getRunScript(request);
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
     * @param parentQueueId
     */
    @Override
    public void executeNextCollection(String parentQueueId) {
        testPlanExecuteService.collectionExecuteQueueFinish(parentQueueId);
    }
}
