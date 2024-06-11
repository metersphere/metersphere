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
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.dto.request.TestPlanApiCaseBatchRunRequest;
import io.metersphere.plan.mapper.ExtTestPlanApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private ApiExecuteService apiExecuteService;
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
    private TestPlanMapper testPlanMapper;

    /**
     * 异步批量执行
     *
     * @param request
     * @param userId
     */
    public void asyncBatchRun(TestPlanApiCaseBatchRunRequest request, String userId) {
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
            if (StringUtils.equals(request.getRunModeConfig().getRunMode(), ApiBatchRunMode.PARALLEL.name())) {
                parallelExecute(request, userId);
            } else {
                serialExecute(request, userId);
            }
        } catch (Exception e) {
            LogUtils.error("批量执行用例失败: ", e);
        }
    }

    /**
     * 串行批量执行
     *
     * @param request
     */
    public void serialExecute(TestPlanApiCaseBatchRunRequest request, String userId) throws Exception {
        List<String> ids = testPlanApiCaseService.doSelectIds(request);
        // todo 查询测试规划
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.API_CASE.name(), userId);

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());

        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行批量执行
     *
     * @param request
     */
    public void parallelExecute(TestPlanApiCaseBatchRunRequest request, String userId) {
        List<String> ids = testPlanApiCaseService.doSelectIds(request);

        // todo 查询测试规划
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

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
        Map<String, String> caseReportMap = initParallelReport(runModeConfig, testPlanApiCases, apiCaseMap, userId);

        List<TaskItem> taskItems = new ArrayList<>(ids.size());

        // 这里ID顺序和队列的ID顺序保持一致
        for (String id : ids) {
            String reportId = caseReportMap.get(id);
            TaskItem taskItem = apiExecuteService.getTaskItem(reportId, id);
            taskItem.setRequestCount(1L);
            taskItems.add(taskItem);
        }

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(testPlan.getProjectId(), runModeConfig);
        taskRequest.setTaskItems(taskItems);
        apiExecuteService.batchExecute(taskRequest);
    }

    private Map<String, String> initParallelReport(ApiRunModeConfigDTO runModeConfig, List<TestPlanApiCase> testPlanApiCases, Map<String, ApiTestCase> caseMap, String userId) {
        // 初始化非集成报告
        List<ApiTestCaseRecord> apiTestCaseRecords = initApiReport(runModeConfig, testPlanApiCases, caseMap, userId);
        return apiTestCaseRecords.stream()
                .collect(Collectors.toMap(ApiTestCaseRecord::getApiTestCaseId, ApiTestCaseRecord::getApiReportId));
    }

    private ApiReportStep getApiReportStep(ApiTestCase apiTestCase, String reportId, long sort) {
        ApiReportStep apiReportStep = new ApiReportStep();
        apiReportStep.setReportId(reportId);
        apiReportStep.setStepId(apiTestCase.getId());
        apiReportStep.setSort(sort);
        apiReportStep.setName(apiTestCase.getName());
        apiReportStep.setStepType(ApiExecuteResourceType.API_CASE.name());
        return apiReportStep;
    }

    private ApiRunModeConfigDTO getRunModeConfig(TestPlanApiCaseBatchRunRequest request) {
        ApiRunModeConfigDTO runModeConfig = BeanUtils.copyBean(new ApiRunModeConfigDTO(), request.getRunModeConfig());
        return runModeConfig;
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
        String reportId = initApiReport(runModeConfig, List.of(testPlanApiCase), Map.of(apiTestCase.getId(), apiTestCase), queue.getUserId()).get(0).getApiReportId();
        TaskRequestDTO taskRequest = getTaskRequestDTO(reportId, apiTestCase, runModeConfig);
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskItem().setRequestCount(1L);

        apiExecuteService.execute(taskRequest);
    }

    private TaskRequestDTO getTaskRequestDTO(String reportId, ApiTestCase apiTestCase, ApiRunModeConfigDTO runModeConfig) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, apiTestCase.getId());
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
    public List<ApiTestCaseRecord> initApiReport(ApiRunModeConfigDTO runModeConfig, List<TestPlanApiCase> testPlanApiCases,
                                                 Map<String, ApiTestCase> caseMap, String userId) {
        List<ApiReport> apiReports = new ArrayList<>();
        List<ApiTestCaseRecord> apiTestCaseRecords = new ArrayList<>();
        List<ApiReportStep> apiReportSteps = new ArrayList<>();

        for (TestPlanApiCase testPlanApiCase : testPlanApiCases) {
            ApiTestCase apiTestCase = caseMap.get(testPlanApiCase.getApiCaseId());
            // 初始化报告
            ApiReport apiReport = getApiReport(runModeConfig, testPlanApiCase, apiTestCase, userId);
            apiReports.add(apiReport);
            // 创建报告和用例的关联关系
            ApiTestCaseRecord apiTestCaseRecord = apiTestCaseService.getApiTestCaseRecord(apiTestCase, apiReport);
            apiTestCaseRecords.add(apiTestCaseRecord);
            apiReportSteps.add(getApiReportStep(apiTestCase, apiReport.getId(), 1));
        }
        apiReportService.insertApiReport(apiReports, apiTestCaseRecords);
        apiReportService.insertApiReportStep(apiReportSteps);
        return apiTestCaseRecords;
    }


    private ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiCase testPlanApiCase, ApiTestCase apiTestCase, String userId) {
        ApiReport apiReport = apiTestCaseBatchRunService.getApiReport(runModeConfig, apiTestCase, userId);
        apiReport.setEnvironmentId(getEnvId(runModeConfig, testPlanApiCase));
        apiReport.setTestPlanCaseId(testPlanApiCase.getId());
        return apiReport;
    }

    public String getEnvId(ApiRunModeConfigDTO runModeConfig, TestPlanApiCase testPlanApiCase) {
        return StringUtils.isBlank(runModeConfig.getEnvironmentId()) ? testPlanApiCase.getEnvironmentId() : runModeConfig.getEnvironmentId();
    }
}
