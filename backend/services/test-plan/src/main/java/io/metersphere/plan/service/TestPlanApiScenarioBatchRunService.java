package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.TestPlanApiScenarioBatchRunDTO;
import io.metersphere.plan.dto.request.TestPlanApiScenarioBatchRunRequest;
import io.metersphere.plan.mapper.ExtTestPlanApiScenarioMapper;
import io.metersphere.plan.mapper.TestPlanApiScenarioMapper;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.CommonBeanFactory;
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
public class TestPlanApiScenarioBatchRunService {
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiScenarioBatchRunService apiScenarioBatchRunService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;

    /**
     * 批量执行
     *
     * @param request
     * @param userId
     */
    public void batchRun(TestPlanApiScenarioBatchRunRequest request, String userId) {
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        testPlanService.setActualStartTime(request.getTestPlanId());
        List<TestPlanApiScenarioBatchRunDTO> testPlanApiScenarios = getSelectIdAndCollectionId(request);
        Set<String> hasCaseCollections = testPlanApiScenarios.stream()
                .map(TestPlanApiScenarioBatchRunDTO::getTestPlanCollectionId)
                .collect(Collectors.toSet());

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());

        // 初始化任务
        ExecTask execTask = initExecTask(testPlanApiScenarios.size(), project, userId, request.getTestPlanId());

        // 初始化任务项
        initExecTaskItem(testPlanApiScenarios, userId, project, execTask, testPlan.getId());

        Thread.startVirtualThread(() ->
                doRun(execTask, request.getTestPlanId(), project, userId, hasCaseCollections, false));
    }

    private void doRun(ExecTask execTask, String testPlanId, Project project, String userId, Set<String> hasCaseCollections, boolean isRerun) {
        List<TestPlanCollection> testPlanCollections = getTestPlanCollections(testPlanId);
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

        TestPlanCollection finalRootCollection = rootCollection;
        List<TestPlanCollection> finalTestPlanCollections = testPlanCollections;

        List<String> execCollectionIds = finalTestPlanCollections.stream().map(TestPlanCollection::getId).toList();
        if (apiBatchRunBaseService.isParallel(finalRootCollection.getExecuteMethod())) {
            //  记录并行执行测试集，用于统计整体执行情况
            apiExecutionSetService.initSet(execTask.getId(), execCollectionIds);

            // 并行执行测试集
            for (TestPlanCollection collection : finalTestPlanCollections) {
                ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(finalRootCollection, collection);
                if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
                    //  并行执行测试集中的用例
                    parallelExecute(execTask.getId(), collection.getId(), runModeConfig, null, execTask.getId(), project, userId, isRerun);
                } else {
                    // 串行执行测试集中的用例
                    serialExecute(execTask.getId(), collection.getId(), runModeConfig, null, execTask.getId(), userId, isRerun);
                }
            }
        } else {
            // 生成测试集队列
            ExecutionQueue collectionQueue = apiBatchRunBaseService.initExecutionqueue(execTask.getId(), execCollectionIds, null,
                    ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name(), null, userId, isRerun);

            executeNextCollection(collectionQueue.getQueueId(), isRerun);
        }
    }

    private List<TestPlanCollection> getTestPlanCollections(String testPlanId) {
        TestPlanCollectionExample example = new TestPlanCollectionExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(testPlanId)
                .andTypeEqualTo(CaseType.SCENARIO_CASE.getKey());
        List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(example);
        return testPlanCollections;
    }

    private Map<String, List<TestPlanApiScenarioBatchRunDTO>> getCollectionMap(List<TestPlanApiScenarioBatchRunDTO> testPlanApiScenarios) {
        Map<String, List<TestPlanApiScenarioBatchRunDTO>> collectionMap = new HashMap<>();
        for (TestPlanApiScenarioBatchRunDTO testPlanApiScenario : testPlanApiScenarios) {
            collectionMap.putIfAbsent(testPlanApiScenario.getTestPlanCollectionId(), new ArrayList<>());
            collectionMap.get(testPlanApiScenario.getTestPlanCollectionId())
                    .add(testPlanApiScenario);
        }
        return collectionMap;
    }

    public void executeNextCollection(String collectionQueueId, boolean isRerun) {
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
            parallelExecute(collectionQueueId, collectionId, runModeConfig, collectionQueueId, null, project, userId, isRerun);
        } else {
            serialExecute(collectionQueueId, collectionId, runModeConfig, collectionQueueId, null, userId, isRerun);
        }
    }

    public void stopCollectionOnFailure(String collectionQueueId) {
        apiExecutionQueueService.deleteQueue(collectionQueueId);
    }

    /**
     * 串行批量执行
     */
    public void serialExecute(String taskId,
                              String collectionId,
                              ApiRunModeConfigDTO runModeConfig,
                              String parentQueueId,
                              String parentSetId,
                              String userId,
                              boolean isRerun) {

        // 初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionQueue(taskId, taskId + '_' + collectionId, runModeConfig, ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name(), parentQueueId, parentSetId, userId);

        List<ExecTaskItem> execTaskItems = apiBatchRunBaseService.getExecTaskItemByTaskIdAndCollectionId(taskId, collectionId, isRerun);

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
                                String userId, boolean isRerun) {

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

        List<ExecTaskItem> execTaskItems = apiBatchRunBaseService.getExecTaskItemByTaskIdAndCollectionId(taskId, collectionId, isRerun);
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

            if (StringUtils.isBlank(parentSetId)) {
                // 如果有没有父集合，则初始化执行集合，以便判断是否执行完毕
                apiExecutionSetService.initSet(taskInfo.getSetId(), taskItemIds);
            }
            taskRequest.setTaskItems(taskItems);
            try {
                apiExecuteService.batchExecute(taskRequest);
            } catch (Exception e) {
                // 执行失败，删除执行集合中的任务项
                if (StringUtils.isBlank(parentSetId)) {
                    apiExecutionSetService.removeItems(taskInfo.getSetId(), taskItemIds);
                }
                LogUtils.error(e);
            }
            taskRequest.setTaskItems(null);
        });
    }

    private ExecTask initExecTask(int caseSize, Project project, String userId, String testPlanId) {
        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(Long.valueOf(caseSize));
        execTask.setTaskName(Translator.get("api_scenario_batch_task_name"));
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.BATCH.name());
        execTask.setTaskType(ExecTaskType.TEST_PLAN_API_SCENARIO_BATCH.name());
        execTask.setResourceId(testPlanId);
        baseTaskHubService.insertExecTask(execTask);
        return execTask;
    }

    private List<ExecTaskItem> initExecTaskItem(List<TestPlanApiScenarioBatchRunDTO> apiTestCases, String userId, Project project, ExecTask execTask, String testPlanId) {
        List<ExecTaskItem> execTaskItems = new ArrayList<>(apiTestCases.size());
        for (TestPlanApiScenarioBatchRunDTO testPlanApiScenario : apiTestCases) {
            ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(execTask.getId(), project.getId(), userId);
            execTaskItem.setOrganizationId(project.getOrganizationId());
            execTaskItem.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name());
            execTaskItem.setResourceId(testPlanApiScenario.getId());
            execTaskItem.setCaseId(testPlanApiScenario.getCaseId());
            execTaskItem.setTaskOrigin(testPlanId);
            execTaskItem.setResourceName(testPlanApiScenario.getName());
            execTaskItem.setCollectionId(testPlanApiScenario.getTestPlanCollectionId());
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
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(queueDetail.getResourceId());
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());

        TaskRequestDTO taskRequest = getTaskRequestDTO(apiScenario.getProjectId(), queue.getRunModeConfig());
        TaskItem taskItem = apiExecuteService.getTaskItem(queueDetail.getResourceId());
        taskItem.setId(queueDetail.getTaskItemId());
        taskRequest.setTaskItem(taskItem);
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());
        taskRequest.getTaskInfo().setParentSetId(queue.getParentSetId());

        try {
            apiExecuteService.execute(taskRequest);
        } catch (Exception e) {
            // 执行失败，删除队列
            apiExecutionQueueService.deleteQueue(queue.getQueueId());
            apiExecutionQueueService.deleteQueue(queue.getParentQueueId());
        }
    }

    public TaskRequestDTO getTaskRequestDTO(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId, runModeConfig);
        taskRequest.setTaskInfo(taskInfo);
        return taskRequest;
    }

    public List<TestPlanApiScenarioBatchRunDTO> getSelectIdAndCollectionId(TestPlanApiScenarioBatchRunRequest request) {
        if (request.isSelectAll()) {
            List<TestPlanApiScenarioBatchRunDTO> testPlanApiCases = extTestPlanApiScenarioMapper.getSelectIdAndCollectionId(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                testPlanApiCases.removeAll(request.getExcludeIds());
            }
            return testPlanApiCases;
        } else {
            return getBatchRunInfo(request.getSelectIds());
        }
    }

    private List<TestPlanApiScenarioBatchRunDTO> getBatchRunInfo(List<String> ids) {
        List<TestPlanApiScenarioBatchRunDTO> testPlanApiScenarios = new ArrayList<>();
        SubListUtils.dealForSubList(ids, 200, (subIds) -> testPlanApiScenarios.addAll(extTestPlanApiScenarioMapper.getBatchRunInfoByIds(subIds)));

        Map<String, TestPlanApiScenarioBatchRunDTO> testPlanApiCaseMap = testPlanApiScenarios
                .stream()
                .collect(Collectors.toMap(TestPlanApiScenarioBatchRunDTO::getId, Function.identity()));

        testPlanApiScenarios.clear();
        // 按ID的顺序排序
        for (String id : ids) {
            TestPlanApiScenarioBatchRunDTO testPlanApiCase = testPlanApiCaseMap.get(id);
            testPlanApiScenarios.add(testPlanApiCase);
        }
        return testPlanApiScenarios;
    }

    public TaskBatchRequestDTO getTaskBatchRequestDTO(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskBatchRequestDTO taskRequest = new TaskBatchRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId, runModeConfig);
        taskRequest.setTaskInfo(taskInfo);
        return taskRequest;
    }

    private TaskInfo getTaskInfo(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskInfo taskInfo = apiScenarioBatchRunService.getTaskInfo(projectId, runModeConfig);
        taskInfo.setBatch(true);
        taskInfo.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name());
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

    public void rerun(ExecTask execTask, String userId) {
        String planId = execTask.getResourceId();
        Set<String> rerunCollectionIds = extExecTaskItemMapper.selectRerunCollectionIds(execTask.getId());
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        doRun(execTask, planId, project, userId, rerunCollectionIds, true);
    }
}
