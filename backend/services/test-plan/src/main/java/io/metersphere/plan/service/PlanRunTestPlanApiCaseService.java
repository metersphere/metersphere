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
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanReportApiCase;
import io.metersphere.plan.domain.TestPlanReportApiCaseExample;
import io.metersphere.plan.mapper.ExtTestPlanReportApiCaseMapper;
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
public class PlanRunTestPlanApiCaseService {
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
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
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private TestPlanApiCaseBatchRunService testPlanApiCaseBatchRunService;
    @Resource
    private TestPlanReportApiCaseMapper testPlanReportApiCaseMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
    @Resource
    private ExtTestPlanReportApiCaseMapper extTestPlanReportApiCaseMapper;

    /**
     * 串行批量执行
     *
     * @Return 是否执行完毕
     */
    public boolean serialExecute(TestPlanExecutionQueue testPlanExecutionQueue) {
        String userId = testPlanExecutionQueue.getCreateUser();
        String parentQueueId = testPlanExecutionQueue.getQueueId();
        TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);

        List<String> ids = extTestPlanReportApiCaseMapper.getIdsByReportIdAndCollectionId(testPlanExecutionQueue.getPrepareReportId(), collection.getId());

        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.PLAN_RUN_API_CASE.name(), parentQueueId, userId);

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
        String parentQueueId = testPlanExecutionQueue.getQueueId();
        String testPlanReportId = testPlanExecutionQueue.getPrepareReportId();
        String userId = testPlanExecutionQueue.getCreateUser();
        TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
        String testPlanId = collection.getTestPlanId();

        List<TestPlanReportApiCase> testPlanReportApiCases = getTestPlanReportApiCases(testPlanReportId, collection);
        testPlanReportApiCases.stream().sorted(Comparator.comparing(TestPlanReportApiCase::getPos));
        if (CollectionUtils.isEmpty(testPlanReportApiCases)) {
            return true;
        }

        List<ApiTestCase> apiTestCases = new ArrayList<>(testPlanReportApiCases.size());

        List<String> caseIds = testPlanReportApiCases.stream()
                .map(TestPlanReportApiCase::getApiCaseId).collect(Collectors.toList());

        // 分批查询
        SubListUtils.dealForSubList(caseIds, 100, subIds -> apiTestCases.addAll(extApiTestCaseMapper.getApiCaseExecuteInfoByIds(subIds)));

        Map<String, ApiTestCase> apiCaseMap = apiTestCases.stream()
                .collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));

        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);
        // 初始化报告，返回用例和报告的 map
        Map<String, String> caseReportMap = initApiReport(runModeConfig, testPlanReportApiCases, apiCaseMap, userId);

        List<TaskItem> taskItems = new ArrayList<>(testPlanReportApiCases.size());

        // 这里ID顺序和队列的ID顺序保持一致
        Iterator<TestPlanReportApiCase> iterator = testPlanReportApiCases.iterator();
        while (iterator.hasNext()) {
            TestPlanReportApiCase testPlanReportApiCase = iterator.next();
            String id = testPlanReportApiCase.getId();
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
            apiExecutionSetService.initSet(parentQueueId, testPlanReportApiCases.stream().map(TestPlanReportApiCase::getId).toList());
        }

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        TaskBatchRequestDTO taskRequest = apiTestCaseBatchRunService.getTaskBatchRequestDTO(testPlan.getProjectId(), runModeConfig);
        taskRequest.setTaskItems(taskItems);
        taskRequest.getTaskInfo().setParentQueueId(parentQueueId);
        taskRequest.getTaskInfo().setUserId(userId);
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.PLAN_RUN_API_CASE.name());
        apiExecuteService.batchExecute(taskRequest);

        return false;
    }

    private List<TestPlanReportApiCase> getTestPlanReportApiCases(String testPlanReportId, TestPlanCollection collection) {
        TestPlanReportApiCaseExample example = new TestPlanReportApiCaseExample();
        example.createCriteria()
                .andTestPlanReportIdEqualTo(testPlanReportId)
                .andTestPlanCollectionIdEqualTo(collection.getId());
        return testPlanReportApiCaseMapper.selectByExample(example);
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

        // 独立报告，执行到当前任务时初始化报告
        String reportId = initApiReport(runModeConfig, List.of(testPlanReportApiCase), Map.of(apiTestCase.getId(), apiTestCase), queue.getUserId()).get(resourceId);
        TaskRequestDTO taskRequest = testPlanApiCaseBatchRunService.getTaskRequestDTO(reportId, testPlanReportApiCase.getId(), apiTestCase, runModeConfig);
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.PLAN_RUN_API_CASE.name());
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());
        taskRequest.getTaskItem().setRequestCount(1L);

        apiExecuteService.execute(taskRequest);
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @return
     */
    public Map<String, String> initApiReport(ApiRunModeConfigDTO runModeConfig, List<TestPlanReportApiCase> testPlanReportApiCases,
                                             Map<String, ApiTestCase> caseMap, String userId) {

        Map<String, String> resourceReportMap = new HashMap<>();
        List<ApiReport> apiReports = new ArrayList<>();
        List<ApiTestCaseRecord> apiTestCaseRecords = new ArrayList<>();
        List<ApiReportStep> apiReportSteps = new ArrayList<>();

        for (TestPlanReportApiCase testPlanReportApiCase : testPlanReportApiCases) {
            ApiTestCase apiTestCase = caseMap.get(testPlanReportApiCase.getApiCaseId());
            // 初始化报告
            ApiReport apiReport = getApiReport(runModeConfig, testPlanReportApiCase, apiTestCase, userId);
            // 报告ID预生成
            apiReport.setId(testPlanReportApiCase.getApiCaseExecuteReportId());
            apiReports.add(apiReport);

            // 标记是测试计划整体执行
            apiReport.setPlan(true);

            // 创建报告和用例的关联关系
            ApiTestCaseRecord apiTestCaseRecord = apiTestCaseService.getApiTestCaseRecord(apiTestCase, apiReport);
            apiTestCaseRecords.add(apiTestCaseRecord);
            apiReportSteps.add(testPlanApiCaseBatchRunService.getApiReportStep(testPlanReportApiCase.getId(), apiTestCase, apiReport.getId(), 1));
            resourceReportMap.put(testPlanReportApiCase.getId(), apiReport.getId());
        }
        apiReportService.insertApiReport(apiReports, apiTestCaseRecords);
        apiReportService.insertApiReportStep(apiReportSteps);
        return resourceReportMap;
    }

    private ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, TestPlanReportApiCase testPlanReportApiCase, ApiTestCase apiTestCase, String userId) {
        ApiReport apiReport = apiTestCaseBatchRunService.getApiReport(runModeConfig, apiTestCase, userId);
        apiReport.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanReportApiCase.getEnvironmentId()));
        apiReport.setTestPlanCaseId(testPlanReportApiCase.getTestPlanApiCaseId());
        return apiReport;
    }

    /**
     * 获取执行脚本
     */
    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        TestPlanReportApiCase testPlanReportApiCase = testPlanReportApiCaseMapper.selectByPrimaryKey(taskItem.getResourceId());
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(testPlanReportApiCase.getApiCaseId());
        return apiTestCaseService.getRunScript(request, apiTestCase);
    }
}
