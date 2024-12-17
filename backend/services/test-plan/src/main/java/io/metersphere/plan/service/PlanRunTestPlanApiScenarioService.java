package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanReportApiScenario;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.mapper.TestPlanReportApiScenarioMapper;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.dto.queue.TestPlanExecutionQueue;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.ExecTaskItem;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlanRunTestPlanApiScenarioService {
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
    @Resource
    private TestPlanApiScenarioBatchRunService testPlanApiScenarioBatchRunService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    /**
     * 串行批量执行
     *
     * @Return 是否执行完毕
     */
    public boolean serialExecute(TestPlanExecutionQueue testPlanExecutionQueue) {
        String userId = testPlanExecutionQueue.getCreateUser();
        TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);

        String taskId = testPlanExecutionQueue.getTaskId();
        String collectionId = collection.getId();
        String execQueueId = taskId + "_" + collectionId;

        List<ExecTaskItem> execTaskItems = apiBatchRunBaseService.getExecTaskItemByTaskIdAndCollectionId(taskId, collectionId, testPlanExecutionQueue.isRerun());
        if (CollectionUtils.isEmpty(execTaskItems)) {
            return true;
        }

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.getExecutionQueue(runModeConfig, ApiExecuteResourceType.PLAN_RUN_API_SCENARIO.name(),
                testPlanExecutionQueue.getTaskId(), userId);
        queue.setQueueId(execQueueId);
        queue.setRerun(testPlanExecutionQueue.isRerun());
        queue.setParentQueueId(testPlanExecutionQueue.getQueueId());
        apiExecutionQueueService.insertQueue(queue);

        apiBatchRunBaseService.initQueueDetail(queue, execTaskItems);

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
        executeNextTask(queue, nextDetail);

        return false;
    }

    /**
     * 并行批量执行
     *
     * @return 是否执行完毕
     */
    public boolean parallelExecute(TestPlanExecutionQueue testPlanExecutionQueue) {
        String userId = testPlanExecutionQueue.getCreateUser();
        TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
        String testPlanId = collection.getTestPlanId();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        String collectionId = collection.getId();
        String taskId = testPlanExecutionQueue.getTaskId();
        String execSetId = taskId + "_" + collectionId;
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);

        TaskBatchRequestDTO taskRequest = testPlanApiScenarioBatchRunService.getTaskBatchRequestDTO(testPlan.getProjectId(), runModeConfig);
        taskRequest.getTaskInfo().setTaskId(testPlanExecutionQueue.getTaskId());
        taskRequest.getTaskInfo().setParentQueueId(testPlanExecutionQueue.getQueueId());
        taskRequest.getTaskInfo().setSetId(execSetId);
        taskRequest.getTaskInfo().setUserId(userId);
        taskRequest.getTaskInfo().setRerun(testPlanExecutionQueue.isRerun());
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.PLAN_RUN_API_SCENARIO.name());

        List<ExecTaskItem> execTaskItems = apiBatchRunBaseService.getExecTaskItemByTaskIdAndCollectionId(testPlanExecutionQueue.getTaskId(),
                collection.getId(), testPlanExecutionQueue.isRerun());

        if (CollectionUtils.isEmpty(execTaskItems)) {
            return true;
        }

        SubListUtils.dealForSubList(execTaskItems, ApiBatchRunBaseService.BATCH_TASK_ITEM_SIZE, subExecTaskItems -> {
            List<TaskItem> taskItems = subExecTaskItems
                    .stream()
                    .map((execTaskItem) -> {
                        TaskItem taskItem = apiExecuteService.getTaskItem(execTaskItem.getResourceId());
                        taskItem.setId(execTaskItem.getId());
                        return taskItem;
                    })
                    .collect(Collectors.toList());

            List<String> taskItemIds = taskItems.stream().map(TaskItem::getId).toList();

            // 初始化执行集合，以便判断是否执行完毕
            apiExecutionSetService.initSet(execSetId, taskItemIds);
            taskRequest.setTaskItems(taskItems);
            try {
                apiExecuteService.batchExecute(taskRequest);
            } catch (Exception e) {
                // 执行失败，删除执行集合中的任务项
                apiExecutionSetService.removeItems(execSetId, taskItemIds);
                LogUtils.error(e);
            }
            taskRequest.setTaskItems(null);
        });
        return false;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String initReport(GetRunScriptRequest request,
                                           TestPlanReportApiScenario testPlanReportApiScenario,
                                           ApiScenario apiScenario) {
        // 初始化报告
        ApiScenarioReport apiScenarioReport = apiScenarioRunService.getScenarioReport(apiScenario, request);
        apiScenarioReport.setName(testPlanReportApiScenario.getApiScenarioName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setEnvironmentId(apiBatchRunBaseService.getEnvId(request.getRunModeConfig(), testPlanReportApiScenario.getEnvironmentId()));
        apiScenarioReport.setPlan(true);
        apiScenarioReport.setTestPlanScenarioId(testPlanReportApiScenario.getTestPlanApiScenarioId());
        apiScenarioReportService.insertApiScenarioReport(apiScenarioReport);
        return apiScenarioRunService.initApiScenarioReportDetail(request.getTaskItem().getId(), apiScenario.getId(), apiScenarioReport.getId());
    }

    /**
     * 执行串行的下一个任务
     *
     * @param queue
     * @param queueDetail
     */
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        String resourceId = queueDetail.getResourceId();

        TestPlanReportApiScenario testPlanReportApiScenario = testPlanReportApiScenarioMapper.selectByPrimaryKey(resourceId);

        if (testPlanReportApiScenario == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", resourceId);
            return;
        }

        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(testPlanReportApiScenario.getApiScenarioId());

        TaskRequestDTO taskRequest = testPlanApiScenarioBatchRunService.getTaskRequestDTO(apiScenario.getProjectId(), queue.getRunModeConfig());
        TaskItem taskItem = apiExecuteService.getTaskItem(queueDetail.getResourceId());
        taskItem.setId(queueDetail.getTaskItemId());
        taskRequest.setTaskItem(taskItem);
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskInfo().setRerun(queue.getRerun());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.PLAN_RUN_API_SCENARIO.name());
        try {
            apiExecuteService.execute(taskRequest);
        } catch (Exception e) {
            // 执行失败，删除队列
            apiExecutionQueueService.deleteQueue(queue.getQueueId());
        }
    }
}
