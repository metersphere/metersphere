package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiTestCaseBatchRunRequest;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseBatchRunService {
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ApiReportDetailMapper apiReportDetailMapper;
    @Resource
    private ApiReportStepMapper apiReportStepMapper;

    /**
     * 异步批量执行
     *
     * @param request
     * @param userId
     */
    public void asyncBatchRun(ApiTestCaseBatchRunRequest request, String userId) {
        Thread.startVirtualThread(() -> batchRun(request, userId));
    }

    /**
     * 批量执行
     *
     * @param request
     * @param userId
     */
    private void batchRun(ApiTestCaseBatchRunRequest request, String userId) {
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
    public void serialExecute(ApiTestCaseBatchRunRequest request, String userId) {
        List<String> ids = apiTestCaseService.doSelectIds(request, false);
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

        if (runModeConfig.isIntegratedReport()) {
            // 初始化集成报告
            initIntegratedReport(runModeConfig, ids, userId, request.getProjectId());

            List<ApiTestCase> apiTestCases = new ArrayList<>(ids.size());

            // 分批查询
            SubListUtils.dealForSubList(ids, 100, subIds -> apiTestCases.addAll(extApiTestCaseMapper.getApiCaseExecuteInfoByIds(subIds)));

            Map<String, ApiTestCase> apiCaseMap = apiTestCases.stream()
                    .collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));

            // 初始化集成报告步骤
            initApiReportSteps(ids, apiCaseMap, runModeConfig.getCollectionReport().getReportId());
        }

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.API_CASE.name(), userId);

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());

        // 集成报告，执行前先设置成 RUNNING
        setRunningIntegrateReport(runModeConfig);

        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行批量执行
     *
     * @param request
     */
    public void parallelExecute(ApiTestCaseBatchRunRequest request, String userId) {
        List<String> ids = apiTestCaseService.doSelectIds(request, false);

        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

        if (runModeConfig.isIntegratedReport()) {
            // 初始化集成报告
            ApiReport apiReport = initIntegratedReport(runModeConfig, ids, userId, request.getProjectId());
            // 集成报告才需要初始化执行集合，用于统计整体执行情况
            apiExecutionSetService.initSet(apiReport.getId(), ids);
        }

        List<ApiTestCase> apiTestCases = new ArrayList<>(ids.size());

        // 分批查询
        SubListUtils.dealForSubList(ids, 100, subIds -> apiTestCases.addAll(extApiTestCaseMapper.getApiCaseExecuteInfoByIds(subIds)));

        Map<String, ApiTestCase> apiCaseMap = apiTestCases.stream()
                .collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));

        // 初始化报告，返回用例和报告的 map
        Map<String, String> caseReportMap = initParallelReport(ids, runModeConfig, apiTestCases, apiCaseMap, userId);

        // 集成报告，执行前先设置成 RUNNING
        setRunningIntegrateReport(runModeConfig);
        List<TaskItem> taskItems = new ArrayList<>(ids.size());

        // 这里ID顺序和队列的ID顺序保持一致
        for (String id : ids) {
            ApiTestCase apiTestCase = apiCaseMap.get(id);

            if (apiTestCase == null) {
                if (runModeConfig.isIntegratedReport()) {
                    // 用例不存在，则在执行集合中删除
                    apiExecutionSetService.removeItem(runModeConfig.getCollectionReport().getReportId(), id);
                }
                LogUtils.info("当前执行任务的用例已删除 {}", id);
                break;
            }

            // 如果是集成报告则生成唯一的虚拟ID，非集成报告使用单用例的报告ID
            String reportId = runModeConfig.isIntegratedReport() ? UUID.randomUUID().toString() : caseReportMap.get(id);

            TaskItem taskItem = apiExecuteService.getTaskItem(reportId, id);
            taskItem.setRequestCount(1L);
            taskItems.add(taskItem);
        }

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(request.getProjectId(), runModeConfig);
        taskRequest.setTaskItems(taskItems);
        apiExecuteService.batchExecute(taskRequest);
    }

    /**
     * 集成报告，执行前先设置成 RUNNING
     *
     * @param runModeConfig
     */
    private void setRunningIntegrateReport(ApiRunModeConfigDTO runModeConfig) {
        if (runModeConfig.isIntegratedReport()) {
            apiReportService.updateReportStatus(runModeConfig.getCollectionReport().getReportId(), ExecStatus.RUNNING.name());
        }
    }

    private Map<String, String> initParallelReport(List<String> ids, ApiRunModeConfigDTO runModeConfig, List<ApiTestCase> apiTestCases, Map<String, ApiTestCase> apiCaseMap, String userId) {
        // 先初始化所有报告
        if (runModeConfig.isIntegratedReport()) {
            // 获取集成报告ID
            String integratedReportId = runModeConfig.getCollectionReport().getReportId();
            initApiReportSteps(ids, apiCaseMap, integratedReportId);
            return null;
        } else {
            // 初始化非集成报告
            List<ApiTestCaseRecord> apiTestCaseRecords = initApiReport(runModeConfig, apiTestCases, userId);
            return apiTestCaseRecords.stream()
                    .collect(Collectors.toMap(ApiTestCaseRecord::getApiTestCaseId, ApiTestCaseRecord::getApiReportId));
        }
    }

    /**
     * 初始化集成报告的报告步骤
     *
     * @param ids
     * @param apiCaseMap
     * @param reportId
     */
    private void initApiReportSteps(List<String> ids, Map<String, ApiTestCase> apiCaseMap, String reportId) {
        AtomicLong sort = new AtomicLong(1);
        List<ApiReportStep> apiReportSteps = ids.stream()
                .map(id -> getApiReportStep(apiCaseMap.get(id), reportId, sort.getAndIncrement()))
                .collect(Collectors.toList());
        apiReportService.insertApiReportStep(apiReportSteps);
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

    private ApiRunModeConfigDTO getRunModeConfig(ApiTestCaseBatchRunRequest request) {
        ApiRunModeConfigDTO runModeConfig = BeanUtils.copyBean(new ApiRunModeConfigDTO(), request.getRunModeConfig());
        if (StringUtils.isNotBlank(request.getRunModeConfig().getIntegratedReportName()) && runModeConfig.isIntegratedReport()) {
            runModeConfig.setCollectionReport(new CollectionReportDTO());
            runModeConfig.getCollectionReport().setReportName(request.getRunModeConfig().getIntegratedReportName());
        }
        return runModeConfig;
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param ids
     * @return
     */
    private ApiReport initIntegratedReport(ApiRunModeConfigDTO runModeConfig, List<String> ids, String userId, String projectId) {
        ApiReport apiReport = getApiReport(runModeConfig, userId);
        apiReport.setName(runModeConfig.getCollectionReport().getReportName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiReport.setIntegrated(true);
        apiReport.setProjectId(projectId);
        // 初始化集成报告与用例的关联关系
        List<ApiTestCaseRecord> records = ids.stream().map(id -> {
            ApiTestCaseRecord record = new ApiTestCaseRecord();
            record.setApiReportId(apiReport.getId());
            record.setApiTestCaseId(id);
            return record;
        }).toList();
        apiReportService.insertApiReport(List.of(apiReport), records);
        // 设置集成报告执行参数
        runModeConfig.getCollectionReport().setReportId(apiReport.getId());
        return apiReport;
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

        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(resourceId);

        String reportId;
        if (runModeConfig.isIntegratedReport()) {
            reportId = IDGenerator.nextStr();
        } else {
            // 独立报告，执行到当前任务时初始化报告
            reportId = initApiReport(runModeConfig, List.of(apiTestCase), queue.getUserId()).get(0).getApiReportId();
        }

        if (apiTestCase == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", resourceId);
            return;
        }

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

    public TaskBatchRequestDTO getTaskBatchRequestDTO(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskBatchRequestDTO taskRequest = new TaskBatchRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId, runModeConfig);
        taskRequest.setTaskInfo(taskInfo);
        return taskRequest;
    }

    public TaskInfo getTaskInfo(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskInfo taskInfo = apiTestCaseService.getTaskInfo(projectId, ApiExecuteRunMode.RUN.name());
        return apiBatchRunBaseService.setBatchRunTaskInfoParam(runModeConfig, taskInfo);
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param apiTestCases
     * @return
     */
    public List<ApiTestCaseRecord> initApiReport(ApiRunModeConfigDTO runModeConfig, List<ApiTestCase> apiTestCases, String userId) {
        List<ApiReport> apiReports = new ArrayList<>();
        List<ApiTestCaseRecord> apiTestCaseRecords = new ArrayList<>();
        List<ApiReportStep> apiReportSteps = new ArrayList<>();
        for (ApiTestCase apiTestCase : apiTestCases) {
            // 初始化报告
            ApiReport apiReport = getApiReport(runModeConfig, apiTestCase, userId);
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

    public ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, ApiTestCase apiTestCase, String userId) {
        ApiReport apiReport = getApiReport(runModeConfig, userId);
        apiReport.setEnvironmentId(apiTestCaseService.getEnvId(runModeConfig, apiTestCase.getEnvironmentId()));
        apiReport.setName(apiTestCase.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiReport.setProjectId(apiTestCase.getProjectId());
        apiReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        return apiReport;
    }

    public ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, String userId) {
        ApiReport apiReport = apiTestCaseService.getApiReport(userId);
        apiReport.setEnvironmentId(runModeConfig.getEnvironmentId());
        apiReport.setRunMode(runModeConfig.getRunMode());
        apiReport.setPoolId(runModeConfig.getPoolId());
        apiReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        return apiReport;
    }


    public void updateStopOnFailureApiReport(ExecutionQueue queue) {
        ApiRunModeConfigDTO runModeConfig = queue.getRunModeConfig();
        if (runModeConfig.isIntegratedReport()) {
            // 获取未执行的请求数，更新统计指标
            String reportId = runModeConfig.getCollectionReport().getReportId();
            ApiReport report = apiReportMapper.selectByPrimaryKey(reportId);
            ApiReportDetailExample example = new ApiReportDetailExample();
            example.createCriteria().andReportIdEqualTo(reportId);
            ApiReportStepExample stepExample = new ApiReportStepExample();
            stepExample.createCriteria().andReportIdEqualTo(reportId);
            long total = apiReportStepMapper.countByExample(stepExample);
            long pendCount = total - apiReportDetailMapper.countByExample(example);
            report.setPendingCount(pendCount);
            ApiScenarioReport apiScenarioReport = new ApiScenarioReport();
            BeanUtils.copyBean(apiScenarioReport, report);
            apiScenarioReport = apiBatchRunBaseService.computeRequestRate(apiScenarioReport, total);
            BeanUtils.copyBean(report, apiScenarioReport);
            report.setStatus(ReportStatus.ERROR.name());
            report.setExecStatus(ExecStatus.COMPLETED.name());
            apiReportMapper.updateByPrimaryKeySelective(report);
        }
    }
}
