package io.metersphere.plan.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.definition.ApiTestCaseBatchRunService;
import io.metersphere.api.service.definition.ApiTestCaseRunService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanReportApiCase;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.mapper.TestPlanReportApiCaseMapper;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.dto.queue.TestPlanExecutionQueue;
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
public class PlanRunTestPlanApiCaseService {
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiTestCaseBatchRunService apiTestCaseBatchRunService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ApiTestCaseRunService apiTestCaseRunService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private TestPlanApiCaseBatchRunService testPlanApiCaseBatchRunService;
    @Resource
    private TestPlanReportApiCaseMapper testPlanReportApiCaseMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;

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
        ExecutionQueue queue = apiBatchRunBaseService.getExecutionQueue(runModeConfig, ApiExecuteResourceType.PLAN_RUN_API_CASE.name(),
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
        String collectionId = collection.getId();
        String taskId = testPlanExecutionQueue.getTaskId();
        String execSetId = taskId + "_" + collectionId;
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        TaskBatchRequestDTO taskRequest = apiTestCaseBatchRunService.getTaskBatchRequestDTO(testPlan.getProjectId(), runModeConfig);
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setParentQueueId(testPlanExecutionQueue.getQueueId());
        taskInfo.setSetId(execSetId);
        taskInfo.setRerun(testPlanExecutionQueue.isRerun());
        taskInfo.setUserId(userId);
        taskInfo.setResourceType(ApiExecuteResourceType.PLAN_RUN_API_CASE.name());

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
                        taskItem.setRequestCount(1L);
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


    /**
     * 执行串行的下一个任务
     *
     * @param queue
     * @param queueDetail
     */
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        ApiRunModeConfigDTO runModeConfig = queue.getRunModeConfig();
        String resourceId = queueDetail.getResourceId();

        TestPlanReportApiCase testPlanReportApiCase = testPlanReportApiCaseMapper.selectByPrimaryKey(resourceId);

        if (testPlanReportApiCase == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", resourceId);
            return;
        }

        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(testPlanReportApiCase.getApiCaseId());

        TaskRequestDTO taskRequest = testPlanApiCaseBatchRunService.getTaskRequestDTO(testPlanReportApiCase.getId(), apiTestCase, runModeConfig);
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.PLAN_RUN_API_CASE.name());
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskInfo().setRerun(queue.getRerun());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());
        taskRequest.getTaskItem().setRequestCount(1L);
        taskRequest.getTaskItem().setId(queueDetail.getTaskItemId());

        try {
            apiExecuteService.execute(taskRequest);
        } catch (Exception e) {
            // 执行失败，删除队列
            apiExecutionQueueService.deleteQueue(queue.getQueueId());
        }
    }

    /**
     * 预生成用例的执行报告
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String initApiReport(GetRunScriptRequest request, TestPlanReportApiCase testPlanReportApiCase, ApiTestCase apiTestCase) {
        // 初始化报告
        ApiReport apiReport = apiTestCaseRunService.getApiReport(apiTestCase, request);
        apiReport.setEnvironmentId(apiBatchRunBaseService.getEnvId(request.getRunModeConfig(), testPlanReportApiCase.getEnvironmentId()));
        apiReport.setTestPlanCaseId(testPlanReportApiCase.getTestPlanApiCaseId());
        // 标记是测试计划整体执行
        apiReport.setPlan(true);
        apiReportService.insertApiReport(apiReport);

        // 创建报告和用例的关联关系
        ApiTestCaseRecord apiTestCaseRecord = apiTestCaseRunService.getApiTestCaseRecord(apiTestCase, apiReport.getId());
        //初始化步骤
        ApiReportStep apiReportStep = apiTestCaseRunService.getApiReportStep(testPlanReportApiCase.getId(), apiTestCase.getName(), apiReport.getId(), 1);
        // 创建报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = apiCommonService.getApiReportRelateTask(request.getTaskItem().getId(), apiReport.getId());
        apiReportService.insertApiReportDetail(apiReportStep, apiTestCaseRecord, apiReportRelateTask);
        return apiTestCaseRecord.getApiReportId();
    }
}
