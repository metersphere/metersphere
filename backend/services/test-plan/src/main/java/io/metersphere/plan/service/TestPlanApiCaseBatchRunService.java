package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.definition.ApiTestCaseBatchRunService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.TestPlanApiCaseBatchRunDTO;
import io.metersphere.plan.dto.request.TestPlanApiCaseBatchRequest;
import io.metersphere.plan.dto.request.TestPlanApiCaseBatchRunRequest;
import io.metersphere.plan.mapper.ExtTestPlanApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import io.metersphere.system.service.BaseTaskHubService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseBatchRunService {
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiTestCaseBatchRunService apiTestCaseBatchRunService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;
    @Resource
    private TestPlanService testPlanService;

    /**
     * 批量执行
     *
     * @param request
     * @param userId
     */
    public void batchRun(TestPlanApiCaseBatchRunRequest request, String userId) {
        testPlanService.setActualStartTime(request.getTestPlanId());
        List<TestPlanApiCaseBatchRunDTO> testPlanApiCases = getSelectIdAndCollectionId(request);
        Set<String> hasCaseCollections = testPlanApiCases.stream()
                .map(TestPlanApiCaseBatchRunDTO::getTestPlanCollectionId)
                .collect(Collectors.toSet());

        List<TestPlanCollection> testPlanCollections = getTestPlanCollections(request.getTestPlanId());
        Iterator<TestPlanCollection> iterator = testPlanCollections.iterator();
        TestPlanCollection rootCollection = new TestPlanCollection();
        while (iterator.hasNext()) {
            TestPlanCollection collection = iterator.next();
            if (StringUtils.equals(collection.getParentId(), CommonConstants.DEFAULT_NULL_VALUE)) {
                // 获取根测试集
                rootCollection = collection;
                iterator.remove();
            } else if (!hasCaseCollections.contains(collection.getId())) {
                // 过滤掉没用的测试集
                iterator.remove();
            }
        }

        // 测试集排序
        testPlanCollections = testPlanCollections.stream()
                .sorted(Comparator.comparingLong(TestPlanCollection::getPos))
                .collect(Collectors.toList());

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());

        // 初始化任务
        ExecTask execTask = initExecTask(testPlanApiCases.size(), project, userId);
        // 初始化任务项
        initExecTaskItem(testPlanApiCases, userId, project, execTask);

        TestPlanCollection finalRootCollection = rootCollection;
        List<TestPlanCollection> finalTestPlanCollections = testPlanCollections;

        Thread.startVirtualThread(() -> {
            List<String> execCollectionIds = finalTestPlanCollections.stream().map(TestPlanCollection::getId).toList();
            if (apiBatchRunBaseService.isParallel(finalRootCollection.getExecuteMethod())) {
                //  记录并行执行测试集，用于统计整体执行情况
                apiExecutionSetService.initSet(execTask.getId(), execCollectionIds);

                // 并行执行测试集
                for (TestPlanCollection collection : finalTestPlanCollections) {
                    ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(finalRootCollection, collection);
                    if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
                        //  并行执行测试集中的用例
                        parallelExecute(execTask.getId(), collection.getId(), runModeConfig, null, execTask.getId(), project, userId);
                    } else {
                        // 串行执行测试集中的用例
                        serialExecute(execTask.getId(), collection.getId(), runModeConfig, null, execTask.getId(), userId);
                    }
                }
            } else {
                // 生成测试集队列
                ExecutionQueue collectionQueue = apiBatchRunBaseService.initExecutionqueue(execTask.getId(), execCollectionIds, null,
                        ApiExecuteResourceType.TEST_PLAN_API_CASE.name(), null, userId);

                executeNextCollection(collectionQueue.getQueueId());
            }
        });
    }

    public void executeNextCollection(String collectionQueueId) {
        ExecutionQueue collectionQueue = apiExecutionQueueService.getQueue(collectionQueueId);
        if (collectionQueue == null) {
            // 失败停止，或者执行完成，更新任务状态
            apiBatchRunBaseService.updateTaskCompletedStatus(collectionQueueId);
            return;
        }
        String userId = collectionQueue.getUserId();
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(collectionQueueId);
        String collectionId = nextDetail.getResourceId();
        TestPlanCollection collection = testPlanCollectionMapper.selectByPrimaryKey(collectionId);

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(collection.getTestPlanId());
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());

        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);
        if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
            parallelExecute(collectionQueueId, collectionId, runModeConfig, collectionQueueId, null, project, userId);
        } else {
            serialExecute(collectionQueueId, collectionId, runModeConfig, collectionQueueId, null, userId);
        }
    }

    public void stopCollectionOnFailure(String collectionQueueId) {
        apiExecutionQueueService.deleteQueue(collectionQueueId);
    }

    private List<TestPlanCollection> getTestPlanCollections(String testPlanId) {
        TestPlanCollectionExample example = new TestPlanCollectionExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(testPlanId)
                .andTypeEqualTo(CaseType.API_CASE.getKey());
        return testPlanCollectionMapper.selectByExample(example);
    }

    /**
     * 串行批量执行
     */
    public void serialExecute(String taskId,
                              String collectionId,
                              ApiRunModeConfigDTO runModeConfig,
                              String parentQueueId,
                              String parentSetId,
                              String userId) {

        // 初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionQueue(taskId, taskId + '_' + collectionId, runModeConfig, ApiExecuteResourceType.TEST_PLAN_API_CASE.name(), parentQueueId, parentSetId, userId);

        List<ExecTaskItem> execTaskItems = apiBatchRunBaseService.getExecTaskItemByTaskIdAndCollectionId(taskId, collectionId);

        apiBatchRunBaseService.initQueueDetail(queue, execTaskItems);

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());

        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行批量执行
     */
    public void parallelExecute(String taskId,
                                String collectionId,
                                ApiRunModeConfigDTO runModeConfig,
                                String parentQueueId,
                                String parentSetId,
                                Project project,
                                String userId) {

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(project.getId(), runModeConfig);
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setUserId(userId);
        if (StringUtils.isNotBlank(parentQueueId)) {
            // 测试集串行
            taskInfo.setSetId(parentQueueId);
            taskInfo.setParentQueueId(parentQueueId);
        } else if (StringUtils.isNotBlank(parentSetId)) {
            // 测试集并行
            taskRequest.getTaskInfo().setSetId(parentSetId + "_" + collectionId);
            taskRequest.getTaskInfo().setParentSetId(parentSetId);
        }

        List<ExecTaskItem> execTaskItems = apiBatchRunBaseService.getExecTaskItemByTaskIdAndCollectionId(taskId, collectionId);

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

            List<String> taskIds = taskItems.stream().map(TaskItem::getId).toList();

            if (StringUtils.isBlank(parentSetId)) {
                // 如果有没有父集合，则初始化执行集合，以便判断是否执行完毕
                apiExecutionSetService.initSet(taskInfo.getSetId(), taskIds);
            }
            taskRequest.setTaskItems(taskItems);
            apiExecuteService.batchExecute(taskRequest);
            taskRequest.setTaskItems(null);
        });
    }

    private ExecTask initExecTask(int caseSize, Project project, String userId) {
        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(Long.valueOf(caseSize));
        execTask.setTaskName(Translator.get("api_batch_task_name"));
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.BATCH.name());
        execTask.setTaskType(ExecTaskType.TEST_PLAN_API_CASE_BATCH.name());
        baseTaskHubService.insertExecTask(execTask);
        return execTask;
    }

    public List<TestPlanApiCaseBatchRunDTO> getSelectIdAndCollectionId(TestPlanApiCaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<TestPlanApiCaseBatchRunDTO> testPlanApiCases = extTestPlanApiCaseMapper.getSelectIdAndCollectionId(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                testPlanApiCases.removeAll(request.getExcludeIds());
            }
            return testPlanApiCases;
        } else {
            return getBatchRunInfo(request.getSelectIds());
        }
    }

    private List<TestPlanApiCaseBatchRunDTO> getBatchRunInfo(List<String> ids) {
        List<TestPlanApiCaseBatchRunDTO> testPlanApiCases = new ArrayList<>();
        SubListUtils.dealForSubList(ids, 200, (subIds) -> testPlanApiCases.addAll(extTestPlanApiCaseMapper.getBatchRunInfoByIds(subIds)));

        Map<String, TestPlanApiCaseBatchRunDTO> testPlanApiCaseMap = testPlanApiCases.stream()
                .collect(Collectors.toMap(TestPlanApiCaseBatchRunDTO::getId, Function.identity()));

        testPlanApiCases.clear();
        // 按ID的顺序排序
        for (String id : ids) {
            TestPlanApiCaseBatchRunDTO testPlanApiCase = testPlanApiCaseMap.get(id);
            testPlanApiCases.add(testPlanApiCase);
        }
        return testPlanApiCases;
    }

    private List<ExecTaskItem> initExecTaskItem(List<TestPlanApiCaseBatchRunDTO> apiTestCases, String userId, Project project, ExecTask execTask) {
        List<ExecTaskItem> execTaskItems = new ArrayList<>(apiTestCases.size());
        for (TestPlanApiCaseBatchRunDTO apiTestCase : apiTestCases) {
            ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(execTask.getId(), project.getId(), userId);
            execTaskItem.setOrganizationId(project.getOrganizationId());
            execTaskItem.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
            execTaskItem.setResourceId(apiTestCase.getId());
            execTaskItem.setCaseId(apiTestCase.getCaseId());
            execTaskItem.setResourceName(apiTestCase.getName());
            execTaskItem.setCollectionId(apiTestCase.getTestPlanCollectionId());
            execTaskItems.add(execTaskItem);
        }
        baseTaskHubService.insertExecTaskDetail(execTaskItems);
        return execTaskItems;
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

        TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(resourceId);

        if (testPlanApiCase == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", resourceId);
            return;
        }

        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(testPlanApiCase.getApiCaseId());
        TaskRequestDTO taskRequest = getTaskRequestDTO(testPlanApiCase.getId(), apiTestCase, runModeConfig);
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());
        taskRequest.getTaskInfo().setParentSetId(queue.getParentSetId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskItem().setId(queueDetail.getTaskItemId());
        taskRequest.getTaskItem().setRequestCount(1L);

        apiExecuteService.execute(taskRequest);
    }

    public TaskRequestDTO getTaskRequestDTO(String resourceId, ApiTestCase apiTestCase, ApiRunModeConfigDTO runModeConfig) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        TaskItem taskItem = apiExecuteService.getTaskItem(resourceId);
        TaskInfo taskInfo = getTaskInfo(apiTestCase.getProjectId(), runModeConfig);
        taskRequest.setTaskInfo(taskInfo);
        taskRequest.setTaskItem(taskItem);
        return taskRequest;
    }

    private TaskBatchRequestDTO getTaskBatchRequestDTO(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskBatchRequestDTO taskRequest = new TaskBatchRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId, runModeConfig);
        taskInfo.setTriggerMode(TaskTriggerMode.BATCH.name());
        taskRequest.setTaskInfo(taskInfo);
        return taskRequest;
    }

    private TaskInfo getTaskInfo(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskInfo taskInfo = apiTestCaseBatchRunService.getTaskInfo(projectId, runModeConfig);
        taskInfo.setBatch(true);
        taskInfo.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        return taskInfo;
    }

    public void finishParallelCollection(String parentSetId, String collectionId) {
        // 并行，移除执行集合中的元素
        Long setSize = apiExecutionSetService.removeItem(parentSetId, collectionId);
        if (setSize == null || setSize == 0) {
            // 执行完成，更新任务状态
            apiBatchRunBaseService.updateTaskCompletedStatus(parentSetId);
        }
    }
}
