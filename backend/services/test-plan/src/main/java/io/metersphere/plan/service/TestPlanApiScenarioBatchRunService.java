package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.request.ApiExecutionMapService;
import io.metersphere.plan.dto.request.TestPlanApiScenarioBatchRunRequest;
import io.metersphere.plan.mapper.*;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.CaseType;
import io.metersphere.sdk.constants.CommonConstants;
import io.metersphere.sdk.constants.TaskTriggerMode;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
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
    private TestPlanApiScenarioService testPlanApiScenarioService;
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
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
    @Resource
    private ApiExecutionMapService apiExecutionMapService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    /**
     * 异步批量执行
     *
     * @param request
     * @param userId
     */
    public void asyncBatchRun(TestPlanApiScenarioBatchRunRequest request, String userId) {
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        testPlanService.setActualStartTime(request.getTestPlanId());
        testPlanService.setTestPlanUnderway(request.getTestPlanId());
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
            List<TestPlanApiScenario> testPlanApiCases = testPlanApiScenarioService.getSelectIdAndCollectionId(request);
            // 按照 testPlanCollectionId 分组, value 为测试计划用例 ID 列表
            Map<String, List<String>> collectionMap = getCollectionMap(testPlanApiCases);

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

            if (apiBatchRunBaseService.isParallel(rootCollection.getExecuteMethod())) {
                // 并行执行测试集
                for (TestPlanCollection collection : testPlanCollections) {
                    List<String> ids = collectionMap.get(collection.getId());
                    ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(rootCollection, collection);
                    if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
                        //  并行执行测试集中的用例
                        parallelExecute(ids, runModeConfig, testPlan.getProjectId(), null, userId);
                    } else {
                        // 串行执行测试集中的用例
                        serialExecute(ids, runModeConfig, null, userId);
                    }
                }
            } else {
                // 串行执行测试集
                List<String> serialCollectionIds = testPlanCollections.stream().map(TestPlanCollection::getId).toList();
                // 生成测试集队列
                ExecutionQueue collectionQueue = apiBatchRunBaseService.initExecutionqueue(serialCollectionIds,
                        ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name(), userId);
                // 记录各测试集中要执行的用例
                apiExecutionMapService.initMap(collectionQueue.getQueueId(), collectionMap);

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

    private Map<String, List<String>> getCollectionMap(List<TestPlanApiScenario> testPlanApiScenarios) {
        Map<String, List<String>> collectionMap = new HashMap<>();
        for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
            collectionMap.putIfAbsent(testPlanApiScenario.getTestPlanCollectionId(), new ArrayList<>());
            List<String> ids = collectionMap.get(testPlanApiScenario.getTestPlanCollectionId());
            ids.add(testPlanApiScenario.getId());
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
        TestPlanCollection collection = testPlanCollectionMapper.selectByPrimaryKey(collectionId);
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);
        if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
            String testPlanId = collection.getTestPlanId();
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
            parallelExecute(ids, runModeConfig, testPlan.getProjectId(), collectionQueueId, userId);
        } else {
            serialExecute(ids, runModeConfig, collectionQueueId, userId);
        }
    }

    public void stopCollectionOnFailure(String collectionQueueId) {
        apiExecutionQueueService.deleteQueue(collectionQueueId);
    }

    /**
     * 串行批量执行
     *
     */
    public void serialExecute(List<String> ids, ApiRunModeConfigDTO runModeConfig, String parentQueueId, String userId) {
        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name(), parentQueueId, userId);
        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行批量执行
     *
     */
    public void parallelExecute(List<String> ids, ApiRunModeConfigDTO runModeConfig, String projectId, String parentQueueId, String userId) {

        Map<String, String> scenarioReportMap = initReport(ids, runModeConfig, userId);

        List<TaskItem> taskItems = ids.stream()
                .map(id -> apiExecuteService.getTaskItem(scenarioReportMap.get(id), id)).toList();

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(projectId, runModeConfig);
        taskRequest.setTaskItems(taskItems);
        taskRequest.getTaskInfo().setUserId(userId);
        taskRequest.getTaskInfo().setParentQueueId(parentQueueId);

        apiExecuteService.batchExecute(taskRequest);
    }

    private Map<String, String> initReport(List<String> ids, ApiRunModeConfigDTO runModeConfig, String userId) {
        List<TestPlanApiScenario> testPlanApiScenarios = new ArrayList<>(ids.size());

        List<ApiScenario> apiScenarios = new ArrayList<>(ids.size());
        // 分批查询
        List<TestPlanApiScenario> finalTestPlanApiScenarios = testPlanApiScenarios;
        SubListUtils.dealForSubList(ids, 100, subIds -> finalTestPlanApiScenarios.addAll(extTestPlanApiScenarioMapper.getScenarioExecuteInfoByIds(subIds)));

        List<String> caseIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getApiScenarioId).toList();
        List<ApiScenario> finalApiScenarios = apiScenarios;
        SubListUtils.dealForSubList(caseIds, 100, subIds -> finalApiScenarios.addAll(extApiScenarioMapper.getScenarioExecuteInfoByIds(subIds)));

        Map<String, ApiScenario> apiScenarioMap = apiScenarios.stream()
                .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));

        Map<String, TestPlanApiScenario> testPlanApiScenarioMap = testPlanApiScenarios.stream()
                .collect(Collectors.toMap(TestPlanApiScenario::getId, Function.identity()));

        testPlanApiScenarios = new ArrayList<>(ids.size());
        for (String id : ids) {
            // 按照ID顺序排序
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMap.get(id);
            if (testPlanApiScenario == null) {
                break;
            }
            testPlanApiScenarios.add(testPlanApiScenario);
        }
        // 初始化独立报告，执行时初始化步骤
        return initScenarioReport(runModeConfig, testPlanApiScenarios, apiScenarioMap, userId);
    }

    public Map<String, String> initScenarioReport(ApiRunModeConfigDTO runModeConfig, List<TestPlanApiScenario> testPlanApiScenarios,
                                                  Map<String, ApiScenario> apiScenarioMap, String userId) {
        List<ApiScenarioReport> apiScenarioReports = new ArrayList<>(testPlanApiScenarios.size());
        List<ApiScenarioRecord> apiScenarioRecords = new ArrayList<>(testPlanApiScenarios.size());
        Map<String, String> resourceReportMap = new HashMap<>();
        for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
            ApiScenario apiScenario = apiScenarioMap.get(testPlanApiScenario.getApiScenarioId());
            // 初始化报告
            ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, testPlanApiScenario, apiScenario, userId);
            apiScenarioReport.setId(IDGenerator.nextStr());
            apiScenarioReports.add(apiScenarioReport);
            // 创建报告和用例的关联关系
            ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(apiScenario, apiScenarioReport);
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

        // 独立报告，执行到当前任务时初始化报告
        String reportId = initScenarioReport(runModeConfig, testPlanApiScenario, apiScenario, queue.getUserId()).getApiScenarioReportId();

        TaskRequestDTO taskRequest = getTaskRequestDTO(apiScenario.getProjectId(), queue.getRunModeConfig());
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, queueDetail.getResourceId());
        taskRequest.setTaskItem(taskItem);
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
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
     * @param apiScenario
     * @return
     */
    public ApiScenarioRecord initScenarioReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiScenario testPlanApiScenario,
                                                ApiScenario apiScenario, String userId) {
        // 初始化报告
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, testPlanApiScenario, apiScenario, userId);
        apiScenarioReport.setId(IDGenerator.nextStr());
        // 创建报告和用例的关联关系
        ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(apiScenario, apiScenarioReport);
        apiScenarioReportService.insertApiScenarioReport(List.of(apiScenarioReport), List.of(apiScenarioRecord));
        return apiScenarioRecord;
    }

    private ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiScenario testPlanApiScenario, ApiScenario apiScenario, String userId) {
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, apiScenario, userId);
        apiScenarioReport.setTestPlanScenarioId(testPlanApiScenario.getId());
        apiScenarioReport.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanApiScenario.getEnvironmentId()));
        return apiScenarioReport;
    }

    public ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, ApiScenario apiScenario, String userId) {
        ApiScenarioReport apiScenarioReport = apiScenarioRunService.getScenarioReport(userId);
        apiScenarioReport.setName(apiScenario.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setProjectId(apiScenario.getProjectId());
        apiScenarioReport.setEnvironmentId(runModeConfig.getEnvironmentId());
        apiScenarioReport.setRunMode(runModeConfig.getRunMode());
        apiScenarioReport.setPoolId(runModeConfig.getPoolId());
        apiScenarioReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        return apiScenarioReport;
    }
}
