package io.metersphere.plan.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.definition.ApiTestCaseBatchRunService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanApiCaseBatchRunDTO;
import io.metersphere.plan.dto.request.ApiExecutionMapService;
import io.metersphere.plan.dto.request.TestPlanApiCaseBatchRequest;
import io.metersphere.plan.dto.request.TestPlanApiCaseBatchRunRequest;
import io.metersphere.plan.mapper.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import io.metersphere.system.service.BaseTaskHubService;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
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
    private ExtApiTestCaseMapper extApiTestCaseMapper;
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
    private ApiExecutionMapService apiExecutionMapService;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiTestCaseBatchRunService apiTestCaseBatchRunService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
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

    /**
     * 异步批量执行
     *
     * @param request
     * @param userId
     */
    public void asyncBatchRun(TestPlanApiCaseBatchRunRequest request, String userId) {
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        testPlanService.setActualStartTime(request.getTestPlanId());
        Thread.startVirtualThread(() -> batchRun(request, userId));
    }

    /**
     * 批量执行
     *
     * @param request
     * @param userId
     */
    private void batchRun(TestPlanApiCaseBatchRunRequest request, String userId) {
        try {
            List<TestPlanApiCaseBatchRunDTO> testPlanApiCases = getSelectIdAndCollectionId(request);
            // 按照 testPlanCollectionId 分组, value 为测试计划用例 ID 列表
            Map<String, List<TestPlanApiCaseBatchRunDTO>> collectionMap = getCollectionMap(testPlanApiCases);

            List<TestPlanCollection> testPlanCollections = getTestPlanCollections(request.getTestPlanId());
            Iterator<TestPlanCollection> iterator = testPlanCollections.iterator();
            TestPlanCollection rootCollection = new TestPlanCollection();
            while (iterator.hasNext()) {
                TestPlanCollection collection = iterator.next();
                if (StringUtils.equals(collection.getParentId(), CommonConstants.DEFAULT_NULL_VALUE)) {
                    // 获取根测试集
                    rootCollection = collection;
                    iterator.remove();
                } else if (!collectionMap.containsKey(collection.getId())) {
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
            List<ExecTaskItem> execTaskItems = initExecTaskItem(testPlanApiCases, userId, project, execTask);

            if (apiBatchRunBaseService.isParallel(rootCollection.getExecuteMethod())) {
                List<String> taskIds = execTaskItems.stream().map(ExecTaskItem::getId).toList();
                // 记录任务项，用于统计整体执行情况
                apiExecutionSetService.initSet(execTask.getId(), taskIds);

                // 并行执行测试集
                for (TestPlanCollection collection : testPlanCollections) {
                    List<TestPlanApiCaseBatchRunDTO> collectionCases = collectionMap.get(collection.getId());
                    ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(rootCollection, collection);
                    if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
                        //  并行执行测试集中的用例
                        parallelExecute(execTask.getId(), collectionCases, runModeConfig, null, execTask.getId(), project, userId);
                    } else {
                        // 串行执行测试集中的用例
                        serialExecute(execTask.getId(),collectionCases, runModeConfig, null, execTask.getId(), userId);
                    }
                }
            } else {
                // 串行执行测试集
                List<String> serialCollectionIds = testPlanCollections.stream().map(TestPlanCollection::getId).toList();
                // 生成测试集队列
                ExecutionQueue collectionQueue = apiBatchRunBaseService.initExecutionqueue(execTask.getId(), serialCollectionIds, null,
                        ApiExecuteResourceType.TEST_PLAN_API_CASE.name(), null, userId);

                Map<String, List<String>> collectionIdMap = collectionMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(TestPlanApiCaseBatchRunDTO::getId).toList()));

                // 记录各测试集中要执行的用例
                apiExecutionMapService.initMap(collectionQueue.getQueueId(), collectionIdMap);

                executeNextCollection(collectionQueue.getQueueId());
            }
        } catch (Exception e) {
            LogUtils.error("批量执行用例失败: ", e);
        }
    }

    public void executeNextCollection(String collectionQueueId) {
        ExecutionQueue collectionQueue = apiExecutionQueueService.getQueue(collectionQueueId);
        if (collectionQueue == null) {
            // 失败停止，或者执行完成，更新任务状态
            apiBatchRunBaseService.updateTaskStatus(collectionQueueId);
            apiBatchRunBaseService.removeRunningTaskCache(collectionQueueId);
            return;
        }
        String userId = collectionQueue.getUserId();
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(collectionQueueId);
        String collectionId = nextDetail.getResourceId();
        List<String> ids = apiExecutionMapService.getAndRemove(collectionQueueId, collectionId);
        List<TestPlanApiCaseBatchRunDTO> testPlanApiCases = getBatchRunInfo(ids);
        TestPlanCollection collection = testPlanCollectionMapper.selectByPrimaryKey(collectionId);

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(collection.getTestPlanId());
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());

        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);
        if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
            parallelExecute(collectionQueueId, testPlanApiCases, runModeConfig, collectionQueueId, null, project, userId);
        } else {
            serialExecute(collectionQueueId, testPlanApiCases, runModeConfig, collectionQueueId, null, userId);
        }
    }

    public void stopCollectionOnFailure(String collectionQueueId) {
        apiExecutionQueueService.deleteQueue(collectionQueueId);
    }

    private Map<String, List<TestPlanApiCaseBatchRunDTO>> getCollectionMap(List<TestPlanApiCaseBatchRunDTO> testPlanApiCases) {
        return testPlanApiCases.stream()
                .collect(Collectors.groupingBy(TestPlanApiCaseBatchRunDTO::getTestPlanCollectionId));
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
     *
     */
    public void serialExecute(String taskId,
                              List<TestPlanApiCaseBatchRunDTO> testPlanApiCases,
                              ApiRunModeConfigDTO runModeConfig,
                              String parentQueueId,
                              String parentSetId,
                              String userId) {

        // 初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionQueue(taskId, null, runModeConfig, ApiExecuteResourceType.TEST_PLAN_API_CASE.name(), parentQueueId, parentSetId, userId);

        List<ExecTaskItem> execTaskItems = new ArrayList<>();
        SubListUtils.dealForSubList(testPlanApiCases, 100,
                subTestPlanReportApiCases-> {
                    List<String> subIds = subTestPlanReportApiCases.stream().map(TestPlanApiCaseBatchRunDTO::getId).toList();
                    execTaskItems.addAll(extExecTaskItemMapper.selectExecInfoByTaskIdAndResourceIds(taskId, subIds));
                });

        // 初始化队列项
        apiBatchRunBaseService.initExecutionQueueDetails(queue.getQueueId(), execTaskItems);

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());

        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行批量执行
     *
     */
    public void parallelExecute(String taskId,
                                List<TestPlanApiCaseBatchRunDTO> testPlanApiCases,
                                ApiRunModeConfigDTO runModeConfig,
                                String parentQueueId,
                                String parentSetId,
                                Project project,
                                String userId) {

        Map<String, String> resourceTaskItemMap = getResourceTaskItemMap(taskId, testPlanApiCases);

        // 初始化报告，返回用例和报告的 map
        Map<String, String> caseReportMap = initApiReport(resourceTaskItemMap, runModeConfig, testPlanApiCases, project.getId(), userId);

        List<TaskItem> taskItems = new ArrayList<>(testPlanApiCases.size());

        // 这里ID顺序和队列的ID顺序保持一致
        Iterator<TestPlanApiCaseBatchRunDTO> iterator = testPlanApiCases.iterator();
        while (iterator.hasNext()) {
            TestPlanApiCaseBatchRunDTO testPlanApiCase = iterator.next();
            String reportId = caseReportMap.get(testPlanApiCase.getId());
            if (StringUtil.isBlank(reportId)) {
                iterator.remove();
                continue;
            }
            TaskItem taskItem = apiExecuteService.getTaskItem(reportId, testPlanApiCase.getId());
            taskItem.setRequestCount(1L);
            taskItem.setId(resourceTaskItemMap.get(testPlanApiCase.getId()));
            taskItems.add(taskItem);
        }

        List<String> taskIds = taskItems.stream().map(TaskItem::getId).toList();

        if (StringUtils.isNotBlank(parentQueueId)) {
            // 如果有父队列，则初始化执行集合，以便判断是否执行完毕
            apiExecutionSetService.initSet(parentQueueId, taskIds);
        }

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(project.getId(), runModeConfig);
        taskRequest.setTaskItems(taskItems);
        taskRequest.getTaskInfo().setTaskId(taskId);
        taskRequest.getTaskInfo().setParentQueueId(parentQueueId);
        taskRequest.getTaskInfo().setParentSetId(parentSetId);
        taskRequest.getTaskInfo().setUserId(userId);
        apiExecuteService.batchExecute(taskRequest);
    }

    private Map<String, String> getResourceTaskItemMap(String taskId, List<TestPlanApiCaseBatchRunDTO> testPlanApiCases) {
        Map<String, String> resourceTaskItemMap = new HashMap<>();
        SubListUtils.dealForSubList(testPlanApiCases, 100,
                subTestPlanReportApiCases-> {
                    List<String> subIds = subTestPlanReportApiCases.stream().map(TestPlanApiCaseBatchRunDTO::getId).toList();
                    extExecTaskItemMapper.selectExecInfoByTaskIdAndResourceIds(taskId, subIds)
                            .forEach(execTaskItem -> resourceTaskItemMap.put(execTaskItem.getResourceId(), execTaskItem.getId()));
                });
        return resourceTaskItemMap;
    }

    private ExecTask initExecTask(int caseSize, Project project, String userId) {
        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(Long.valueOf(caseSize));
        execTask.setTaskName(Translator.get("api_batch_task_name"));
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.MANUAL.name());
        execTask.setTaskType(ExecTaskType.TEST_PLAN_API_CASE.name());
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

        // 查询用例名称信息
        List<String> caseIds = testPlanApiCases.stream().map(TestPlanApiCaseBatchRunDTO::getApiCaseId).collect(Collectors.toList());
        Map<String, String> apiTestCaseNameMap = extApiTestCaseMapper.getNameInfo(caseIds)
                .stream()
                .collect(Collectors.toMap(ApiTestCase::getId, ApiTestCase::getName));

        Map<String, TestPlanApiCaseBatchRunDTO> testPlanApiCaseMap = testPlanApiCases
                .stream()
                .collect(Collectors.toMap(TestPlanApiCaseBatchRunDTO::getId, Function.identity()));

        testPlanApiCases.clear();
        // 按ID的顺序排序
        for (String id : ids) {
            TestPlanApiCaseBatchRunDTO testPlanApiCase = testPlanApiCaseMap.get(id);
            if (testPlanApiCase != null) {
                testPlanApiCase.setName(apiTestCaseNameMap.get(testPlanApiCase.getApiCaseId()));
                testPlanApiCases.add(testPlanApiCase);
            }
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
            execTaskItem.setResourceName(apiTestCase.getName());
            execTaskItems.add(execTaskItem);
        }
        baseTaskHubService.insertExecTaskDetail(execTaskItems);
        return execTaskItems;
    }

    public ApiReportStep getApiReportStep(String resourceId, String name, String reportId, long sort) {
        ApiReportStep apiReportStep = new ApiReportStep();
        apiReportStep.setReportId(reportId);
        apiReportStep.setStepId(resourceId);
        apiReportStep.setSort(sort);
        apiReportStep.setName(name);
        apiReportStep.setStepType(ApiExecuteResourceType.API_CASE.name());
        return apiReportStep;
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
        String testPlanId = testPlanApiCase.getTestPlanId();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);

        // 独立报告，执行到当前任务时初始化报告
        String reportId = initApiReport(Map.of(apiTestCase.getId(), queueDetail.getTaskItemId()), runModeConfig, List.of(BeanUtils.copyBean(new TestPlanApiCaseBatchRunDTO(), testPlanApiCase)),
                testPlan.getProjectId(), queue.getUserId()).get(testPlanApiCase.getId());
        TaskRequestDTO taskRequest = getTaskRequestDTO(reportId, testPlanApiCase.getId(), apiTestCase, runModeConfig);
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());
        taskRequest.getTaskInfo().setParentSetId(queue.getParentSetId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskItem().setId(queueDetail.getTaskItemId());
        taskRequest.getTaskItem().setRequestCount(1L);

        apiExecuteService.execute(taskRequest);
    }

    public TaskRequestDTO getTaskRequestDTO(String reportId, String resourceId, ApiTestCase apiTestCase, ApiRunModeConfigDTO runModeConfig) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, resourceId);
        TaskInfo taskInfo = getTaskInfo(apiTestCase.getProjectId(), runModeConfig);
        taskRequest.setTaskInfo(taskInfo);
        taskRequest.setTaskItem(taskItem);
        return taskRequest;
    }

    private TaskBatchRequestDTO getTaskBatchRequestDTO(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskBatchRequestDTO taskRequest = new TaskBatchRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId, runModeConfig);
        taskRequest.setTaskInfo(taskInfo);
        return taskRequest;
    }

    private TaskInfo getTaskInfo(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskInfo taskInfo = apiTestCaseBatchRunService.getTaskInfo(projectId, runModeConfig);
        taskInfo.setBatch(true);
        taskInfo.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        return taskInfo;
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @return
     */
    public Map<String, String> initApiReport(Map<String, String> resourceExecTaskItemMap, ApiRunModeConfigDTO runModeConfig, List<TestPlanApiCaseBatchRunDTO> testPlanApiCases,
                                                 String projectId, String userId) {
        List<ApiReport> apiReports = new ArrayList<>();
        List<ApiTestCaseRecord> apiTestCaseRecords = new ArrayList<>();
        List<ApiReportRelateTask> apiReportRelateTasks = new ArrayList<>();
        List<ApiReportStep> apiReportSteps = new ArrayList<>();
        Map<String, String> resourceReportMap = new HashMap<>();

        for (TestPlanApiCaseBatchRunDTO testPlanApiCase : testPlanApiCases) {
            // 初始化报告
            ApiReport apiReport = getApiReport(runModeConfig, testPlanApiCase, projectId, userId);
            apiReports.add(apiReport);
            // 创建报告和用例的关联关系
            ApiTestCaseRecord apiTestCaseRecord = apiTestCaseService.getApiTestCaseRecord(testPlanApiCase.getApiCaseId(), apiReport);
            apiTestCaseRecords.add(apiTestCaseRecord);
            apiReportSteps.add(getApiReportStep(testPlanApiCase.getId(), testPlanApiCase.getName(), apiReport.getId(), 1));
            resourceReportMap.put(testPlanApiCase.getId(), apiReport.getId());

            // 创建报告和任务的关联关系
            ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
            apiReportRelateTask.setReportId(apiReport.getId());
            apiReportRelateTask.setTaskResourceId(resourceExecTaskItemMap.get(apiReport.getId()));
            apiReportRelateTasks.add(apiReportRelateTask);
        }
        apiReportService.insertApiReport(apiReports, apiTestCaseRecords, apiReportRelateTasks);
        apiReportService.insertApiReportStep(apiReportSteps);
        return resourceReportMap;
    }

    private ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiCaseBatchRunDTO testPlanApiCase, String projectId, String userId) {
        ApiReport apiReport = apiTestCaseBatchRunService.getApiReport(runModeConfig, userId);
        apiReport.setEnvironmentId(apiTestCaseService.getEnvId(runModeConfig, testPlanApiCase.getEnvironmentId()));
        apiReport.setName(testPlanApiCase.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiReport.setProjectId(projectId);
        apiReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        apiReport.setTestPlanCaseId(testPlanApiCase.getId());
        return apiReport;
    }
}
