package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.domain.ApiScenarioReportStep;
import io.metersphere.api.dto.scenario.ApiScenarioBatchRunRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioBatchRunService {
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioRunService apiScenarioRunService;

    /**
     * 异步批量执行
     *
     * @param request
     * @param userId
     */
    public void asyncBatchRun(ApiScenarioBatchRunRequest request, String userId) {
        Thread.startVirtualThread(() -> batchRun(request, userId));
    }

    /**
     * 批量执行
     *
     * @param request
     * @param userId
     */
    private void batchRun(ApiScenarioBatchRunRequest request, String userId) {
        try {
            if (apiBatchRunBaseService.isParallel(request.getRunModeConfig().getRunMode())) {
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
    public void serialExecute(ApiScenarioBatchRunRequest request, String userId) throws Exception {
        List<String> ids = apiScenarioService.doSelectIds(request, false);
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);
        // 初始化集成报告
        if (runModeConfig.isIntegratedReport()) {
            initIntegratedReport(runModeConfig, ids, userId, request.getProjectId());

            // 初始化集合报告步骤
            initReport(ids, runModeConfig, userId);
        }

        // 集成报告，执行前先设置成 RUNNING
        setRunningIntegrateReport(runModeConfig);

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.API_SCENARIO.name(), userId);
        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行批量执行
     *
     * @param request
     */
    public void parallelExecute(ApiScenarioBatchRunRequest request, String userId) {
        List<String> ids = apiScenarioService.doSelectIds(request, false);

        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

        if (runModeConfig.isIntegratedReport()) {
            // 初始化集成报告
            ApiScenarioReport apiScenarioReport = initIntegratedReport(runModeConfig, ids, userId, request.getProjectId());
            // 集成报告才需要初始化执行集合，用于统计整体执行情况
            apiExecutionSetService.initSet(apiScenarioReport.getId(), ids);
        }

        Map<String, String> scenarioReportMap = initReport(ids, runModeConfig, userId);

        // 集成报告，执行前先设置成 RUNNING
        setRunningIntegrateReport(runModeConfig);

        List<TaskItem> taskItems = ids.stream()
                .map(id -> {
                    String reportId = runModeConfig.isIntegratedReport() ? IDGenerator.nextStr() : scenarioReportMap.get(id);
                    return apiExecuteService.getTaskItem(reportId, id);
                }).toList();
        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(request.getProjectId(), runModeConfig);
        taskRequest.setTaskItems(taskItems);

        apiExecuteService.batchExecute(taskRequest);
    }


    private Map<String, String> initReport(List<String> ids, ApiRunModeConfigDTO runModeConfig, String userId) {
        List<ApiScenario> apiScenarios = new ArrayList<>(ids.size());
        // 分批查询
        List<ApiScenario> finalApiScenarios = apiScenarios;
        SubListUtils.dealForSubList(ids, 100, subIds -> finalApiScenarios.addAll(extApiScenarioMapper.getScenarioExecuteInfoByIds(subIds)));

        Map<String, ApiScenario> apiScenarioMap = apiScenarios.stream()
                .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));
        apiScenarios = new ArrayList<>(ids.size());

        for (String id : ids) {
            // 按照ID顺序排序
            ApiScenario apiScenario = apiScenarioMap.get(id);
            if (apiScenario == null) {
                break;
            }
            apiScenarios.add(apiScenario);
        }

        if (runModeConfig.isIntegratedReport()) {
            // 集合报告初始化一级步骤
            initApiScenarioReportStep(apiScenarios, runModeConfig.getCollectionReport().getReportId());
            return null;
        } else {
            // 非集合报告，初始化独立报告，执行时初始化步骤
            return initScenarioReport(runModeConfig, apiScenarios, userId);
        }
    }


    /**
     * 集成报告，执行前先设置成 RUNNING
     *
     * @param runModeConfig
     */
    private void setRunningIntegrateReport(ApiRunModeConfigDTO runModeConfig) {
        if (runModeConfig.isIntegratedReport()) {
            apiScenarioReportService.updateReportStatus(runModeConfig.getCollectionReport().getReportId(), ExecStatus.RUNNING.name());
        }
    }

    public void initApiScenarioReportStep(List<ApiScenario> apiScenarios, String reportId) {
        AtomicLong sort = new AtomicLong(1);
        List<ApiScenarioReportStep> apiScenarioReportSteps = new ArrayList<>(apiScenarios.size());
        for (ApiScenario apiScenario : apiScenarios) {
            ApiScenarioReportStep apiReportStep = new ApiScenarioReportStep();
            apiReportStep.setReportId(reportId);
            apiReportStep.setStepId(apiScenario.getId());
            apiReportStep.setSort(sort.getAndIncrement());
            apiReportStep.setName(apiScenario.getName());
            apiReportStep.setStepType(ApiExecuteResourceType.API_SCENARIO.name());
            apiScenarioReportSteps.add(apiReportStep);
        }
        if (CollectionUtils.isNotEmpty(apiScenarioReportSteps)) {
            apiScenarioReportService.insertApiScenarioReportStep(apiScenarioReportSteps);
        }
    }

    private ApiRunModeConfigDTO getRunModeConfig(ApiScenarioBatchRunRequest request) {
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
    private ApiScenarioReport initIntegratedReport(ApiRunModeConfigDTO runModeConfig, List<String> ids, String userId, String projectId) {
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, userId);
        apiScenarioReport.setName(runModeConfig.getCollectionReport().getReportName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setIntegrated(true);
        apiScenarioReport.setProjectId(projectId);
        // 初始化集成报告与用例的关联关系
        List<ApiScenarioRecord> records = ids.stream().map(id -> {
            ApiScenarioRecord scenarioRecord = new ApiScenarioRecord();
            scenarioRecord.setApiScenarioReportId(apiScenarioReport.getId());
            scenarioRecord.setApiScenarioId(id);
            return scenarioRecord;
        }).toList();
        apiScenarioReportService.insertApiScenarioReport(List.of(apiScenarioReport), records);
        // 设置集成报告执行参数
        runModeConfig.getCollectionReport().setReportId(apiScenarioReport.getId());
        return apiScenarioReport;
    }

    /**
     * 执行串行的下一个任务
     *
     * @param queue
     * @param queueDetail
     */
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        ApiRunModeConfigDTO runModeConfig = queue.getRunModeConfig();
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(queueDetail.getResourceId());

        ApiScenarioDetail apiScenarioDetail = apiScenarioRunService.getForRun(queueDetail.getResourceId());
        if (apiScenarioDetail == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", queueDetail.getResourceId());
            return;
        }

        String reportId;
        if (runModeConfig.isIntegratedReport()) {
            reportId = IDGenerator.nextStr();
        } else {
            // 独立报告，执行到当前任务时初始化报告
            reportId = initScenarioReport(runModeConfig, apiScenario, queue.getUserId()).getApiScenarioReportId();
        }

        TaskRequestDTO taskRequest = getTaskRequestDTO(apiScenario.getProjectId(), queue.getRunModeConfig());
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, queueDetail.getResourceId());
        taskRequest.setTaskItem(taskItem);
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());

        apiExecuteService.execute(taskRequest);
    }

    private TaskRequestDTO getTaskRequestDTO(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId, runModeConfig);
        taskRequest.setTaskInfo(taskInfo);
        return taskRequest;
    }

    private TaskBatchRequestDTO getTaskBatchRequestDTO(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskBatchRequestDTO taskRequest = new TaskBatchRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId, runModeConfig);
        taskRequest.setTaskInfo(taskInfo);
        return taskRequest;
    }

    public TaskInfo getTaskInfo(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskInfo taskInfo = apiScenarioRunService.getTaskInfo(projectId, ApiExecuteRunMode.RUN.name());
        return apiBatchRunBaseService.setBatchRunTaskInfoParam(runModeConfig, taskInfo);
    }

    public Map<String, String> initScenarioReport(ApiRunModeConfigDTO runModeConfig, List<ApiScenario> apiScenarios, String userId) {
        List<ApiScenarioReport> apiScenarioReports = new ArrayList<>(apiScenarios.size());
        List<ApiScenarioRecord> apiScenarioRecords = new ArrayList<>(apiScenarios.size());
        for (ApiScenario apiScenario : apiScenarios) {
            // 初始化报告
            ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, apiScenario, userId);
            apiScenarioReport.setId(IDGenerator.nextStr());
            apiScenarioReports.add(apiScenarioReport);
            // 创建报告和用例的关联关系
            ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(apiScenario, apiScenarioReport);
            apiScenarioRecords.add(apiScenarioRecord);
        }
        apiScenarioReportService.insertApiScenarioReport(apiScenarioReports, apiScenarioRecords);
        return apiScenarioRecords.stream().collect(Collectors.toMap(ApiScenarioRecord::getApiScenarioId, ApiScenarioRecord::getApiScenarioReportId));
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param apiScenario
     * @return
     */
    public ApiScenarioRecord initScenarioReport(ApiRunModeConfigDTO runModeConfig, ApiScenario apiScenario, String userId) {
        // 初始化报告
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, apiScenario, userId);
        apiScenarioReport.setId(IDGenerator.nextStr());
        // 创建报告和用例的关联关系
        ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(apiScenario, apiScenarioReport);
        apiScenarioReportService.insertApiScenarioReport(List.of(apiScenarioReport), List.of(apiScenarioRecord));
        return apiScenarioRecord;
    }


    private ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, ApiScenario apiScenario, String userId) {
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, userId);
        apiScenarioReport.setEnvironmentId(apiScenarioRunService.getEnvId(runModeConfig, apiScenario.getEnvironmentId()));
        apiScenarioReport.setName(apiScenario.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setProjectId(apiScenario.getProjectId());
        apiScenarioReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        return apiScenarioReport;
    }

    public ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, String userId) {
        ApiScenarioReport apiScenarioReport = apiScenarioRunService.getScenarioReport(userId);
        apiScenarioReport.setEnvironmentId(runModeConfig.getEnvironmentId());
        apiScenarioReport.setRunMode(runModeConfig.getRunMode());
        apiScenarioReport.setPoolId(runModeConfig.getPoolId());
        apiScenarioReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        return apiScenarioReport;
    }


    public void updateStopOnFailureReport(ExecutionQueue queue) {
        ApiRunModeConfigDTO runModeConfig = queue.getRunModeConfig();
        if (BooleanUtils.isFalse(runModeConfig.isIntegratedReport())) {
            return;
        }
        try {
            ExecutionQueueDetail queueDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
            if (queueDetail == null) {
                return;
            }
            long requestCount = 0L;
            while (queueDetail != null) {
                ApiScenarioDetail apiScenarioDetail = apiScenarioRunService.getForRun(queueDetail.getResourceId());
                if (apiScenarioDetail == null) {
                    LogUtils.info("当前场景已删除 {}", queueDetail.getResourceId());
                    continue;
                }

                Long requestCountItem = apiScenarioRunService.getRequestCount(apiScenarioDetail.getSteps());
                requestCount += requestCountItem;

                // 初始化报告步骤
                if (runModeConfig.isIntegratedReport()) {
                    apiScenarioRunService.initScenarioReportSteps(apiScenarioDetail.getId(), apiScenarioDetail.getSteps(), runModeConfig.getCollectionReport().getReportId());
                }

                queueDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
            }

            // 获取未执行的请求数，更新统计指标
            String reportId = runModeConfig.getCollectionReport().getReportId();
            ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
            Long pendingCount = requestCount + report.getPendingCount();
            report.setPendingCount(pendingCount);
            // 计算各种通过率
            long total = apiScenarioReportService.getRequestTotal(report);
            report = apiBatchRunBaseService.computeRequestRate(report, total);
            report.setStatus(ReportStatus.ERROR.name());
            report.setExecStatus(ExecStatus.COMPLETED.name());
            apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        } catch (Exception e) {
            LogUtils.error("失败停止，补充报告步骤失败：", e);
        }
    }
}
