package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.TestPlanApiScenarioBatchRunDTO;
import io.metersphere.plan.dto.request.ApiExecutionMapService;
import io.metersphere.plan.dto.request.TestPlanApiScenarioBatchRunRequest;
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
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.uid.IDGenerator;
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
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioBatchRunService apiScenarioBatchRunService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
    @Resource
    private ApiExecutionMapService apiExecutionMapService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private BaseTaskHubService baseTaskHubService;

    /**
     * 异步批量执行
     *
     * @param request
     * @param userId
     */
    public void asyncBatchRun(TestPlanApiScenarioBatchRunRequest request, String userId) {
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
    private void batchRun(TestPlanApiScenarioBatchRunRequest request, String userId) {
        try {
            List<TestPlanApiScenarioBatchRunDTO> testPlanApiScenarios = getSelectIdAndCollectionId(request);
            // 按照 testPlanCollectionId 分组, value 为测试计划用例 ID 列表
            Map<String, List<TestPlanApiScenarioBatchRunDTO>> collectionMap = getCollectionMap(testPlanApiScenarios);

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

            if (apiBatchRunBaseService.isParallel(rootCollection.getExecuteMethod())) {
                // 并行执行测试集
                for (TestPlanCollection collection : testPlanCollections) {
                    List<TestPlanApiScenarioBatchRunDTO> collectionCases = collectionMap.get(collection.getId());
                    ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(rootCollection, collection);
                    if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
                        //  并行执行测试集中的用例
                        parallelExecute(collectionCases, runModeConfig, null, project, userId);
                    } else {
                        // 串行执行测试集中的用例
                        serialExecute(collectionCases, runModeConfig, null, project, userId);
                    }
                }
            } else {
                // 串行执行测试集
                List<String> serialCollectionIds = testPlanCollections.stream().map(TestPlanCollection::getId).toList();
                // 生成测试集队列
                ExecutionQueue collectionQueue = apiBatchRunBaseService.initExecutionqueue(serialCollectionIds,
                        ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name(), userId);

                Map<String, List<String>> collectionIdMap = collectionMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(TestPlanApiScenarioBatchRunDTO::getId).toList()));

                // 记录各测试集中要执行的用例
                apiExecutionMapService.initMap(collectionQueue.getQueueId(), collectionIdMap);

                executeNextCollection(collectionQueue.getQueueId());
            }
        } catch (Exception e) {
            LogUtils.error("批量执行用例失败: ", e);
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

    public void executeNextCollection(String collectionQueueId) {
        ExecutionQueue collectionQueue = apiExecutionQueueService.getQueue(collectionQueueId);
        if (collectionQueue == null) {
            // 失败停止后，队列被删除
            return;
        }
        String userId = collectionQueue.getUserId();
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(collectionQueueId);
        String collectionId = nextDetail.getResourceId();
        List<String> ids = apiExecutionMapService.getAndRemove(collectionQueueId, collectionId);
        List<TestPlanApiScenarioBatchRunDTO> testPlanApiScenarios = getBatchRunInfo(ids);

        TestPlanCollection collection = testPlanCollectionMapper.selectByPrimaryKey(collectionId);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(collection.getTestPlanId());
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());

        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);
        if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
            parallelExecute(testPlanApiScenarios, runModeConfig, collectionQueueId, project, userId);
        } else {
            serialExecute(testPlanApiScenarios, runModeConfig, collectionQueueId, project, userId);
        }
    }

    public void stopCollectionOnFailure(String collectionQueueId) {
        apiExecutionQueueService.deleteQueue(collectionQueueId);
    }

    /**
     * 串行批量执行
     *
     */
    public void serialExecute(List<TestPlanApiScenarioBatchRunDTO> testPlanApiScenarios, ApiRunModeConfigDTO runModeConfig, String parentQueueId, Project project, String userId) {
        // 初始化任务
        ExecTask execTask = initExecTask(testPlanApiScenarios.size(), runModeConfig, project, userId);
        // 初始化任务项
        List<ExecTaskItem> execTaskItems = initExecTaskItem(testPlanApiScenarios, userId, project, execTask);
        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionQueue(execTask.getId(), runModeConfig, ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name(), parentQueueId, userId);
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
    public void parallelExecute(List<TestPlanApiScenarioBatchRunDTO> testPlanApiScenarios, ApiRunModeConfigDTO runModeConfig, String parentQueueId, Project project, String userId) {

        Map<String, String> scenarioReportMap = initReport(testPlanApiScenarios, runModeConfig, project.getId(), userId);

        // 初始化任务
        ExecTask execTask = initExecTask(testPlanApiScenarios.size(), runModeConfig, project, userId);

        // 初始化任务项
        Map<String, String> resourceExecTaskItemMap = initExecTaskItem(testPlanApiScenarios, userId, project, execTask)
                .stream()
                .collect(Collectors.toMap(ExecTaskItem::getResourceId, ExecTaskItem::getId));

        List<TaskItem> taskItems = testPlanApiScenarios.stream()
                .map(testPlanApiScenario -> {
                    String id = testPlanApiScenario.getId();
                    TaskItem taskItem = apiExecuteService.getTaskItem(scenarioReportMap.get(id), id);
                    taskItem.setId(resourceExecTaskItemMap.get(id));
                    return taskItem;
                }).toList();

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(project.getId(), runModeConfig);
        taskRequest.setTaskItems(taskItems);
        taskRequest.getTaskInfo().setTaskId(execTask.getId());
        taskRequest.getTaskInfo().setUserId(userId);
        taskRequest.getTaskInfo().setParentQueueId(parentQueueId);

        apiExecuteService.batchExecute(taskRequest);
    }

    private ExecTask initExecTask(int caseSize, ApiRunModeConfigDTO runModeConfig, Project project, String userId) {
        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(Long.valueOf(caseSize));
        if (runModeConfig.isIntegratedReport()) {
            execTask.setTaskName(runModeConfig.getCollectionReport().getReportName());
        } else {
            execTask.setTaskName(Translator.get("api_batch_task_name"));
        }
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.MANUAL.name());
        execTask.setTaskType(ExecTaskType.TEST_PLAN_API_SCENARIO.name());
        baseTaskHubService.insertExecTask(execTask);
        return execTask;
    }

    private List<ExecTaskItem> initExecTaskItem(List<TestPlanApiScenarioBatchRunDTO> apiTestCases, String userId, Project project, ExecTask execTask) {
        List<ExecTaskItem> execTaskItems = new ArrayList<>(apiTestCases.size());
        for (TestPlanApiScenarioBatchRunDTO testPlanApiScenario : apiTestCases) {
            ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(execTask.getId(), project.getId(), userId);
            execTaskItem.setOrganizationId(project.getOrganizationId());
            execTaskItem.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name());
            execTaskItem.setResourceId(testPlanApiScenario.getId());
            execTaskItem.setResourceName(testPlanApiScenario.getName());
            execTaskItems.add(execTaskItem);
        }
        baseTaskHubService.insertExecTaskDetail(execTaskItems);
        return execTaskItems;
    }

    public Map<String, String> initReport( List<TestPlanApiScenarioBatchRunDTO> testPlanApiScenarios,
                                           ApiRunModeConfigDTO runModeConfig, String projectId, String userId) {
        List<ApiScenarioReport> apiScenarioReports = new ArrayList<>(testPlanApiScenarios.size());
        List<ApiScenarioRecord> apiScenarioRecords = new ArrayList<>(testPlanApiScenarios.size());
        Map<String, String> resourceReportMap = new HashMap<>();
        for (TestPlanApiScenarioBatchRunDTO testPlanApiScenario : testPlanApiScenarios) {
            // 初始化报告
            ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, testPlanApiScenario, projectId, userId);
            apiScenarioReport.setId(IDGenerator.nextStr());
            apiScenarioReports.add(apiScenarioReport);
            // 创建报告和用例的关联关系
            ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(testPlanApiScenario.getApiScenarioId(), apiScenarioReport);
            apiScenarioRecords.add(apiScenarioRecord);
            resourceReportMap.put(testPlanApiScenario.getId(), apiScenarioReport.getId());
        }
        apiScenarioReportService.insertApiScenarioReport(apiScenarioReports, apiScenarioRecords);
        return resourceReportMap;
    }

    /**
     * 执行串行的下一个任务
     *
     * @param queue
     * @param queueDetail
     */
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        ApiRunModeConfigDTO runModeConfig = queue.getRunModeConfig();
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(queueDetail.getResourceId());
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
        String testPlanId = testPlanApiScenario.getTestPlanId();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);

        // 独立报告，执行到当前任务时初始化报告
        String reportId = initScenarioReport(runModeConfig, BeanUtils.copyBean(new TestPlanApiScenarioBatchRunDTO(), testPlanApiScenario), testPlan.getId(), queue.getUserId())
                .getApiScenarioReportId();

        TaskRequestDTO taskRequest = getTaskRequestDTO(apiScenario.getProjectId(), queue.getRunModeConfig());
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, queueDetail.getResourceId());
        taskItem.setId(queueDetail.getTaskItemId());
        taskRequest.setTaskItem(taskItem);
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());

        apiExecuteService.execute(taskRequest);
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

        // 查询用例名称信息
        List<String> caseIds = testPlanApiScenarios.stream().map(TestPlanApiScenarioBatchRunDTO::getApiScenarioId).collect(Collectors.toList());
        Map<String, String> apiScenarioNameMap = extApiScenarioMapper.getNameInfo(caseIds)
                .stream()
                .collect(Collectors.toMap(ApiScenario::getId, ApiScenario::getName));

        Map<String, TestPlanApiScenarioBatchRunDTO> testPlanApiCaseMap = testPlanApiScenarios
                .stream()
                .collect(Collectors.toMap(TestPlanApiScenarioBatchRunDTO::getId, Function.identity()));

        testPlanApiScenarios.clear();
        // 按ID的顺序排序
        for (String id : ids) {
            TestPlanApiScenarioBatchRunDTO testPlanApiCase = testPlanApiCaseMap.get(id);
            if (testPlanApiCase != null) {
                testPlanApiCase.setName(apiScenarioNameMap.get(testPlanApiCase.getApiScenarioId()));
                testPlanApiScenarios.add(testPlanApiCase);
            }
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
        taskInfo.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name());
        return taskInfo;
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param testPlanApiScenario
     * @return
     */
    public ApiScenarioRecord initScenarioReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiScenarioBatchRunDTO testPlanApiScenario, String projectId, String userId) {
        // 初始化报告
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, testPlanApiScenario, projectId, userId);
        apiScenarioReport.setId(IDGenerator.nextStr());
        // 创建报告和用例的关联关系
        ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(testPlanApiScenario.getApiScenarioId(), apiScenarioReport);
        apiScenarioReportService.insertApiScenarioReport(List.of(apiScenarioReport), List.of(apiScenarioRecord));
        return apiScenarioRecord;
    }

    private ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiScenarioBatchRunDTO testPlanApiScenario, String projectId, String userId) {
        ApiScenarioReport apiScenarioReport = apiScenarioRunService.getScenarioReport(userId);
        apiScenarioReport.setName(testPlanApiScenario.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setProjectId(projectId);
        apiScenarioReport.setEnvironmentId(runModeConfig.getEnvironmentId());
        apiScenarioReport.setRunMode(runModeConfig.getRunMode());
        apiScenarioReport.setPoolId(runModeConfig.getPoolId());
        apiScenarioReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        apiScenarioReport.setTestPlanScenarioId(testPlanApiScenario.getId());
        apiScenarioReport.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanApiScenario.getEnvironmentId()));
        return apiScenarioReport;
    }
}
