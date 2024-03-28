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
import io.metersphere.api.dto.scenario.ApiScenarioBatchRunRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.dto.scenario.ApiScenarioParseParam;
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
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ApiBatchRunBaseService apiBatchRunBaseService;

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
                ApiScenarioDetail apiScenarioDetail = apiScenarioService.get(id);
                if (apiScenarioDetail == null) {
                    if (runModeConfig.isIntegratedReport()) {
                        // 用例不存在，则在执行集合中删除
                        apiExecutionSetService.removeItem(runModeConfig.getCollectionReport().getReportId(), id);
                    }
                    LogUtils.info("当前执行任务的用例已删除 {}", id);
                    break;
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
        // 这里ID顺序和队列的ID顺序保持一致
        for (String id : ids) {
            ApiScenarioDetail apiScenarioDetail = apiScenarioService.get(id);
            if (apiScenarioDetail == null) {
                break;
            }
            if (runModeConfig.isIntegratedReport()) {
                // 初始化集成报告步骤
                initIntegratedReportSteps(apiScenarioDetail, runModeConfig.getCollectionReport().getReportId(), sort.getAndIncrement());
            } else {
                // 初始化非集成报告
                String reportId = initScenarioReport(runModeConfig, apiScenarioDetail, userId).getApiScenarioReportId();
                // 初始化报告步骤
                apiScenarioService.initScenarioReportSteps(apiScenarioDetail.getSteps(), reportId);
                scenarioReportMap.put(id, reportId);
            }
        }
        return isIntegratedReport ? null : scenarioReportMap;
    }

    /**
     *  集成报告，执行前先设置成 RUNNING
     * @param runModeConfig
     */
    private void setRunningIntegrateReport(ApiRunModeConfigDTO runModeConfig) {
        if (runModeConfig.isIntegratedReport()) {
            apiScenarioReportService.updateReportStatus(runModeConfig.getCollectionReport().getReportId(), ApiReportStatus.RUNNING.name());
        }
    }

    /**
     * 初始化集成报告的报告步骤
     */
    private void initIntegratedReportSteps(ApiScenarioDetail apiScenarioDetail, String reportId, long sort) {
        // 将当前场景生成一级报告步骤
        ApiScenarioReportStep apiScenarioReportStep = getApiScenarioReportStep(apiScenarioDetail, reportId, sort);
        // 初始化报告步骤
        List<ApiScenarioReportStep> scenarioReportSteps = apiScenarioService.getScenarioReportSteps(apiScenarioReportStep.getStepId(), apiScenarioDetail.getSteps(), reportId);
        scenarioReportSteps.addFirst(apiScenarioReportStep);
        apiScenarioReportService.insertApiScenarioReportStep(scenarioReportSteps);
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
        ApiScenarioDetail apiScenarioDetail = apiScenarioService.get(queueDetail.getResourceId());
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

        taskRequest.setReportId(reportId);

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
        apiScenarioReport.setName(apiScenario.getName() + "_" +  DateUtils.getTimeString(System.currentTimeMillis()));
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

}
