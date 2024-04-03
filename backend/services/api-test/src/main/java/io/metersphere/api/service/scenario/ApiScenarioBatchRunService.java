package io.metersphere.api.service.scenario;

import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.domain.ApiScenarioReportStep;
import io.metersphere.api.dto.ApiScenarioParamConfig;
import io.metersphere.api.dto.ApiScenarioParseTmpParam;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.api.task.CollectionReportDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.*;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    public void serialExecute(ApiScenarioBatchRunRequest request, String userId) throws Exception {
        List<String> ids = apiScenarioService.doSelectIds(request, false);
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);
        // 初始化集成报告
        if (runModeConfig.isIntegratedReport()) {
            initIntegratedReport(runModeConfig, ids, userId, request.getProjectId());
        }

        Map<String, String> scenarioReportMap = initReport(ids, runModeConfig, userId);

        // 集成报告，执行前先设置成 RUNNING
        setRunningIntegrateReport(runModeConfig);

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.API_SCENARIO.name(), scenarioReportMap, userId);
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

        AtomicInteger errorCount = new AtomicInteger();
        // 这里ID顺序和队列的ID顺序保持一致
        for (String id : ids) {

            String reportId = null;
            try {
                ApiScenarioDetail apiScenarioDetail = apiScenarioService.getForRun(id);
                if (apiScenarioDetail == null) {
                    if (runModeConfig.isIntegratedReport()) {
                        // 用例不存在，则在执行集合中删除
                        apiExecutionSetService.removeItem(runModeConfig.getCollectionReport().getReportId(), id);
                    }
                    LogUtils.info("当前执行任务的用例已删除 {}", id);
                    continue;
                }

                if (runModeConfig.isIntegratedReport()) {
                    // 集成报告生成虚拟的报告ID
                    reportId = IDGenerator.nextStr();
                } else {
                    reportId = scenarioReportMap.get(id);
                }

                TaskRequestDTO taskRequest = getTaskRequestDTO(reportId, apiScenarioDetail, runModeConfig);
                execute(taskRequest, apiScenarioDetail);
            } catch (Exception e) {
                LogUtils.error("执行用例失败 {}-{}", reportId, id);
                LogUtils.error(e);
                if (errorCount.getAndIncrement() > 10) {
                    LogUtils.error("批量执行用例失败，错误次数超过10次，停止执行");
                    return;
                }
            }
        }
    }

    private Map<String, String> initReport(List<String> ids, ApiRunModeConfigDTO runModeConfig, String userId) {
        Map<String, String> scenarioReportMap = new HashMap<>();
        Boolean isIntegratedReport = runModeConfig.isIntegratedReport();
        AtomicInteger sort = new AtomicInteger(1);

        List<ApiScenarioReportStep> apiScenarioReportSteps = new ArrayList<>(ids.size());

        List<ApiScenario> apiScenarios = new ArrayList<>(ids.size());
        // 分批查询
        SubListUtils.dealForSubList(ids, 100, subIds -> apiScenarios.addAll(extApiScenarioMapper.getScenarioExecuteInfoByIds(subIds)));

        Map<String, ApiScenario> apiScenarioMap = apiScenarios.stream()
                .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));

        // 这里ID顺序和队列的ID顺序保持一致
        for (String id : ids) {
            ApiScenario apiScenario = apiScenarioMap.get(id);
            if (apiScenario == null) {
                break;
            }

            if (runModeConfig.isIntegratedReport()) {
                // 集合报告初始化一级步骤
                ApiScenarioReportStep apiScenarioReportStep = getApiScenarioReportStep(apiScenario, runModeConfig.getCollectionReport().getReportId(), sort.getAndIncrement());
                apiScenarioReportSteps.add(apiScenarioReportStep);
            } else {
                // 非集合报告，初始化独立报告，执行时初始化步骤
                String reportId = initScenarioReport(runModeConfig, apiScenario, userId).getApiScenarioReportId();
                scenarioReportMap.put(id, reportId);
            }
        }

        if (CollectionUtils.isNotEmpty(apiScenarioReportSteps)) {
            apiScenarioReportService.insertApiScenarioReportStep(apiScenarioReportSteps);
        }

        return isIntegratedReport ? null : scenarioReportMap;
    }


    private Long getRequestCount(List<ApiScenarioStepDTO> steps) {
        AtomicLong requestCount = new AtomicLong();
        apiScenarioService.traversalStepTree(steps, step -> {
            if (BooleanUtils.isTrue(step.getEnable()) && apiScenarioService.isRequestStep(step)) {
                requestCount.getAndIncrement();
            }
            return true;
        });
        return requestCount.get();
    }

    /**
     * 集成报告，执行前先设置成 RUNNING
     *
     * @param runModeConfig
     */
    private void setRunningIntegrateReport(ApiRunModeConfigDTO runModeConfig) {
        if (runModeConfig.isIntegratedReport()) {
            apiScenarioReportService.updateReportStatus(runModeConfig.getCollectionReport().getReportId(), ApiReportStatus.RUNNING.name());
        }
    }

    private ApiScenarioReportStep getApiScenarioReportStep(ApiScenario apiScenario, String reportId, long sort) {
        ApiScenarioReportStep apiReportStep = new ApiScenarioReportStep();
        apiReportStep.setReportId(reportId);
        apiReportStep.setStepId(apiScenario.getId());
        apiReportStep.setSort(sort);
        apiReportStep.setName(apiScenario.getName());
        apiReportStep.setStepType(ApiExecuteResourceType.API_SCENARIO.name());
        return apiReportStep;
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
        ApiScenarioDetail apiScenarioDetail = apiScenarioService.getForRun(queueDetail.getResourceId());
        if (apiScenarioDetail == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", queueDetail.getResourceId());
            return;
        }
        TaskRequestDTO taskRequest = getTaskRequestDTO(queueDetail.getReportId(), apiScenarioDetail, queue.getRunModeConfig());
        taskRequest.setQueueId(queue.getQueueId());
        execute(taskRequest, apiScenarioDetail);
    }

    /**
     * 执行批量的单个任务
     */
    public void execute(TaskRequestDTO taskRequest, ApiScenarioDetail apiScenarioDetail) {
        ApiRunModeConfigDTO runModeConfig = taskRequest.getRunModeConfig();
        String reportId = taskRequest.getReportId();
        String envId = getEnvId(runModeConfig, apiScenarioDetail);
        boolean envGroup = getEnvGroup(runModeConfig, apiScenarioDetail);

        // 解析生成待执行的场景树
        MsScenario msScenario = new MsScenario();
        msScenario.setRefType(ApiScenarioStepRefType.DIRECT.name());
        msScenario.setScenarioConfig(apiScenarioDetail.getScenarioConfig());
        msScenario.setProjectId(apiScenarioDetail.getProjectId());

        ApiScenarioParseParam parseParam = new ApiScenarioParseParam();
        parseParam.setScenarioConfig(apiScenarioDetail.getScenarioConfig());
        parseParam.setStepDetails(Map.of());
        parseParam.setEnvironmentId(envId);
        parseParam.setGrouped(envGroup);

        // 初始化报告步骤
        if (runModeConfig.isIntegratedReport()) {
            apiScenarioService.initScenarioReportSteps(apiScenarioDetail.getId(), apiScenarioDetail.getSteps(), runModeConfig.getCollectionReport().getReportId());
        } else {
            apiScenarioService.initScenarioReportSteps(apiScenarioDetail.getSteps(), reportId);
        }

        taskRequest.setReportId(reportId);
        // 记录请求数量
        taskRequest.setRequestCount(getRequestCount(apiScenarioDetail.getSteps()));

        ApiScenarioParseTmpParam tmpParam = apiScenarioService.parse(msScenario, apiScenarioDetail.getSteps(), parseParam);

        ApiResourceRunRequest runRequest = apiScenarioService.getApiResourceRunRequest(msScenario, tmpParam);

        ApiScenarioParamConfig parseConfig = apiScenarioService.getApiScenarioParamConfig(parseParam, tmpParam.getScenarioParseEnvInfo());
        parseConfig.setReportId(reportId);

        apiExecuteService.execute(runRequest, taskRequest, parseConfig);
    }

    private TaskRequestDTO getTaskRequestDTO(String reportId, ApiScenarioDetail apiScenarioDetail, ApiRunModeConfigDTO runModeConfig) {
        TaskRequestDTO taskRequest = apiScenarioService.getTaskRequest(reportId, apiScenarioDetail.getId(), apiScenarioDetail.getProjectId(), ApiExecuteRunMode.RUN.name());
        taskRequest.setSaveResult(true);
        taskRequest.setRealTime(false);
        taskRequest.setRunModeConfig(runModeConfig);
        runModeConfig.setEnvironmentId(getEnvId(runModeConfig, apiScenarioDetail));
        runModeConfig.setGrouped(getEnvGroup(runModeConfig, apiScenarioDetail));
        return taskRequest;
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
        ApiScenarioRecord apiScenarioRecord = apiScenarioService.getApiScenarioRecord(apiScenario, apiScenarioReport);
        apiScenarioReportService.insertApiScenarioReport(List.of(apiScenarioReport), List.of(apiScenarioRecord));
        return apiScenarioRecord;
    }


    private ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, ApiScenario apiScenario, String userId) {
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, userId);
        apiScenarioReport.setEnvironmentId(getEnvId(runModeConfig, apiScenario));
        apiScenarioReport.setName(apiScenario.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setProjectId(apiScenario.getProjectId());
        apiScenarioReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        return apiScenarioReport;
    }

    public ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, String userId) {
        ApiScenarioReport apiScenarioReport = apiScenarioService.getScenarioReport(userId);
        apiScenarioReport.setEnvironmentId(runModeConfig.getEnvironmentId());
        apiScenarioReport.setRunMode(runModeConfig.getRunMode());
        apiScenarioReport.setPoolId(runModeConfig.getPoolId());
        apiScenarioReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        return apiScenarioReport;
    }

    /**
     * 获取执行的环境ID
     * 优先使用运行配置的环境
     * 没有则使用用例自身的环境
     *
     * @param runModeConfig
     * @param apiScenario
     * @return
     */
    public String getEnvId(ApiRunModeConfigDTO runModeConfig, ApiScenario apiScenario) {
        return StringUtils.isBlank(runModeConfig.getEnvironmentId()) ? apiScenario.getEnvironmentId() : runModeConfig.getEnvironmentId();
    }

    public boolean getEnvGroup(ApiRunModeConfigDTO runModeConfig, ApiScenario apiScenario) {
        return StringUtils.isBlank(runModeConfig.getEnvironmentId()) ? apiScenario.getGrouped() : runModeConfig.getGrouped();
    }

    public void UpdateStopOnFailureReport(ExecutionQueue queue) {
        ApiRunModeConfigDTO runModeConfig = queue.getRunModeConfig();
        try {
            ExecutionQueueDetail queueDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
            if (queueDetail == null) {
                return;
            }
            Long requestCount = 0L;
            while (queueDetail != null) {
                ApiScenarioDetail apiScenarioDetail = apiScenarioService.getForRun(queueDetail.getResourceId());
                if (apiScenarioDetail == null) {
                    LogUtils.info("当前场景已删除 {}", queueDetail.getResourceId());
                    continue;
                }

                Long requestCountItem = getRequestCount(apiScenarioDetail.getSteps());
                requestCount += requestCountItem;

                // 初始化报告步骤
                if (runModeConfig.isIntegratedReport()) {
                    apiScenarioService.initScenarioReportSteps(apiScenarioDetail.getId(), apiScenarioDetail.getSteps(), runModeConfig.getCollectionReport().getReportId());
                } else {
                    apiScenarioService.initScenarioReportSteps(apiScenarioDetail.getSteps(), queueDetail.getReportId());
                }
                queueDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
            }
            if (runModeConfig.isIntegratedReport()) {
                // 获取未执行的请求数，更新统计指标
                String reportId = runModeConfig.getCollectionReport().getReportId();
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
                Long pendingCount = requestCount + report.getPendingCount();
                report.setPendingCount(pendingCount);
                // 计算各种通过率
                report = computeRequestRate(report);
                apiScenarioReportMapper.updateByPrimaryKeySelective(report);
            }
        } catch (Exception e) {
            LogUtils.error("失败停止，补充报告步骤失败：", e);
        }
    }

    public ApiScenarioReport computeRequestRate(ApiScenarioReport report) {
        long total = apiScenarioReportService.getRequestTotal(report);
        // 计算各个概率
        double successRate = calculateRate(report.getSuccessCount(), total);
        double errorRate = calculateRate(report.getErrorCount(), total);
        double pendingRate = calculateRate(report.getPendingCount(), total);
        double fakeErrorRate = calculateRate(report.getFakeErrorCount(), total);

        // 计算总和
        double sum = successRate + errorRate + pendingRate + fakeErrorRate;

        LogUtils.info("偏移总量重新计算", sum);

        // 避免分母为零
        double adjustment = sum > 0 ? 1.0 / sum : 0.0;

        // 调整概率，使总和精确为100%
        successRate *= adjustment;
        errorRate *= adjustment;
        pendingRate *= adjustment;
        fakeErrorRate *= adjustment;

        report.setRequestPassRate(formatRate(successRate));
        report.setRequestErrorRate(formatRate(errorRate));
        report.setRequestPendingRate(formatRate(pendingRate));
        report.setRequestFakeErrorRate(formatRate(fakeErrorRate));

        return report;
    }

    // 计算概率
    private static double calculateRate(long count, double total) {
        return total > 0 ? count / total : 0.0;
    }

    // 格式化概率，保留两位小数
    private static String formatRate(double rate) {
        return String.format("%.2f", rate * 100);
    }

}
