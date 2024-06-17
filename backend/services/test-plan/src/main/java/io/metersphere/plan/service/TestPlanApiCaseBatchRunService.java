package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiReportStep;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseRecord;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.definition.ApiTestCaseBatchRunService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.request.ApiExecutionMapService;
import io.metersphere.plan.dto.request.TestPlanApiCaseBatchRunRequest;
import io.metersphere.plan.mapper.*;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.CaseType;
import io.metersphere.sdk.constants.CommonConstants;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
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
    private TestPlanApiCaseService testPlanApiCaseService;
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
    private ExtTestPlanMapper extTestPlanMapper;

    /**
     * 异步批量执行
     *
     * @param request
     * @param userId
     */
    public void asyncBatchRun(TestPlanApiCaseBatchRunRequest request, String userId) {
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        testPlanService.setTestPlanUnderway(request.getTestPlanId());
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
            List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseService.getSelectIdAndCollectionId(request);
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
                        parallelExecute(ids, runModeConfig, null, testPlan.getProjectId(), userId);
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
                        ApiExecuteResourceType.TEST_PLAN_API_CASE.name(), userId);
                // 记录各测试集中要执行的用例
                apiExecutionMapService.initMap(collectionQueue.getQueueId(), collectionMap);

                executeNextCollection(collectionQueue.getQueueId());
            }
        } catch (Exception e) {
            LogUtils.error("批量执行用例失败: ", e);
        }
    }

    public void executeNextCollection(String collectionQueueId) {
        ExecutionQueue collectionQueue = apiExecutionQueueService.getQueue(collectionQueueId);
        if (collectionQueue == null) {
            // 失败停止后，队列被删除
            return;
        }
        String userId = collectionQueue.getUserId();
        String queueId = collectionQueue.getQueueId();
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queueId);
        String collectionId = nextDetail.getResourceId();
        List<String> ids = apiExecutionMapService.getAndRemove(queueId, collectionId);
        TestPlanCollection collection = testPlanCollectionMapper.selectByPrimaryKey(collectionId);
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);
        if (apiBatchRunBaseService.isParallel(runModeConfig.getRunMode())) {
            String testPlanId = collection.getTestPlanId();
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
            parallelExecute(ids, runModeConfig, queueId, testPlan.getProjectId(), userId);
        } else {
            serialExecute(ids, runModeConfig, queueId, userId);
        }
    }

    public void stopCollectionOnFailure(String collectionQueueId) {
        apiExecutionQueueService.deleteQueue(collectionQueueId);
    }

    private Map<String, List<String>> getCollectionMap(List<TestPlanApiCase> testPlanApiCases) {
        Map<String, List<String>> collectionMap = new HashMap<>();
        for (TestPlanApiCase testPlanApiCase : testPlanApiCases) {
            collectionMap.putIfAbsent(testPlanApiCase.getTestPlanCollectionId(), new ArrayList<>());
            List<String> ids = collectionMap.get(testPlanApiCase.getTestPlanCollectionId());
            ids.add(testPlanApiCase.getId());
        }
        return collectionMap;
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
    public void serialExecute(List<String> ids, ApiRunModeConfigDTO runModeConfig, String parentQueueId, String userId) {
        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.TEST_PLAN_API_CASE.name(), parentQueueId, userId);

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());

        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行批量执行
     *
     */
    public void parallelExecute(List<String> ids, ApiRunModeConfigDTO runModeConfig, String parentQueueId, String projectId, String userId) {

        List<TestPlanApiCase> testPlanApiCases = new ArrayList<>(ids.size());
        List<ApiTestCase> apiTestCases = new ArrayList<>(ids.size());

        // 分批查询
        SubListUtils.dealForSubList(ids, 100, subIds -> testPlanApiCases.addAll(extTestPlanApiCaseMapper.getApiCaseExecuteInfoByIds(subIds)));

        List<String> caseIds = testPlanApiCases.stream()
                .map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());

        // 分批查询
        SubListUtils.dealForSubList(caseIds, 100, subIds -> apiTestCases.addAll(extApiTestCaseMapper.getApiCaseExecuteInfoByIds(subIds)));

        Map<String, ApiTestCase> apiCaseMap = apiTestCases.stream()
                .collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));

        // 初始化报告，返回用例和报告的 map
        Map<String, String> caseReportMap = initApiReport(runModeConfig, testPlanApiCases, apiCaseMap, userId);

        List<TaskItem> taskItems = new ArrayList<>(ids.size());

        // 这里ID顺序和队列的ID顺序保持一致

        Iterator<String> iterator = ids.iterator();
        while (iterator.hasNext()) {
            String id = iterator.next();
            String reportId = caseReportMap.get(id);
            if (StringUtil.isBlank(reportId)) {
                iterator.remove();
                continue;
            }
            TaskItem taskItem = apiExecuteService.getTaskItem(reportId, id);
            taskItem.setRequestCount(1L);
            taskItems.add(taskItem);
        }

        if (StringUtils.isNotBlank(parentQueueId)) {
            // 如果有父队列，则初始化执行集合，以便判断是否执行完毕
            apiExecutionSetService.initSet(parentQueueId, ids);
        }

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(projectId, runModeConfig);
        taskRequest.setTaskItems(taskItems);
        taskRequest.getTaskInfo().setParentQueueId(parentQueueId);
        apiExecuteService.batchExecute(taskRequest);
    }

    public ApiReportStep getApiReportStep(String resourceId, ApiTestCase apiTestCase, String reportId, long sort) {
        ApiReportStep apiReportStep = new ApiReportStep();
        apiReportStep.setReportId(reportId);
        apiReportStep.setStepId(resourceId);
        apiReportStep.setSort(sort);
        apiReportStep.setName(apiTestCase.getName());
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

        // 独立报告，执行到当前任务时初始化报告
        String reportId = initApiReport(runModeConfig, List.of(testPlanApiCase), Map.of(apiTestCase.getId(), apiTestCase), queue.getUserId()).get(testPlanApiCase.getId());
        TaskRequestDTO taskRequest = getTaskRequestDTO(reportId, testPlanApiCase.getId(), apiTestCase, runModeConfig);
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());
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
        taskInfo.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        return taskInfo;
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @return
     */
    public Map<String, String> initApiReport(ApiRunModeConfigDTO runModeConfig, List<TestPlanApiCase> testPlanApiCases,
                                                 Map<String, ApiTestCase> caseMap, String userId) {
        List<ApiReport> apiReports = new ArrayList<>();
        List<ApiTestCaseRecord> apiTestCaseRecords = new ArrayList<>();
        List<ApiReportStep> apiReportSteps = new ArrayList<>();
        Map<String, String> resourceReportMap = new HashMap<>();

        for (TestPlanApiCase testPlanApiCase : testPlanApiCases) {
            ApiTestCase apiTestCase = caseMap.get(testPlanApiCase.getApiCaseId());
            // 初始化报告
            ApiReport apiReport = getApiReport(runModeConfig, testPlanApiCase, apiTestCase, userId);
            apiReports.add(apiReport);
            // 创建报告和用例的关联关系
            ApiTestCaseRecord apiTestCaseRecord = apiTestCaseService.getApiTestCaseRecord(apiTestCase, apiReport);
            apiTestCaseRecords.add(apiTestCaseRecord);
            apiReportSteps.add(getApiReportStep(testPlanApiCase.getId(), apiTestCase, apiReport.getId(), 1));
            resourceReportMap.put(testPlanApiCase.getId(), apiReport.getId());
        }
        apiReportService.insertApiReport(apiReports, apiTestCaseRecords);
        apiReportService.insertApiReportStep(apiReportSteps);
        return resourceReportMap;
    }

    private ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiCase testPlanApiCase, ApiTestCase apiTestCase, String userId) {
        ApiReport apiReport = apiTestCaseBatchRunService.getApiReport(runModeConfig, apiTestCase, userId);
        apiReport.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanApiCase.getEnvironmentId()));
        apiReport.setTestPlanCaseId(testPlanApiCase.getId());
        return apiReport;
    }
}
