package io.metersphere.api.service.scenario;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.request.controller.MsScriptElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.mapper.ApiScenarioBlobMapper;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ApiScenarioReportStepMapper;
import io.metersphere.api.parser.step.StepParser;
import io.metersphere.api.parser.step.StepParserFactory;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.api.processor.TimeWaitingProcessor;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.EnvironmentGroupService;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.service.ApiPluginService;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioRunService {
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiScenarioFileService apiScenarioFileService;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private EnvironmentService environmentService;
    @Resource
    private EnvironmentGroupService environmentGroupService;
    @Resource
    private ApiPluginService apiPluginService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioBlobMapper apiScenarioBlobMapper;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    private ApiScenarioReportStepMapper apiScenarioReportStepMapper;

    public TaskRequestDTO run(String id, String reportId, String userId) {
        ApiScenarioDetail apiScenarioDetail = getForRun(id);

        // 解析生成待执行的场景树
        MsScenario msScenario = getMsScenario(apiScenarioDetail);

        ApiScenarioParseParam parseParam = getApiScenarioParseParam(apiScenarioDetail);

        return executeRun(apiScenarioDetail, msScenario, apiScenarioDetail.getSteps(), parseParam, new ApiResourceRunRequest(), reportId, userId);
    }

    public TaskRequestDTO run(ApiScenarioDebugRequest request, String userId) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());

        // 解析生成待执行的场景树
        MsScenario msScenario = new MsScenario();
        msScenario.setRefType(ApiScenarioStepRefType.DIRECT.name());
        msScenario.setScenarioConfig(getScenarioConfig(request, true));
        msScenario.setProjectId(request.getProjectId());

        List<ApiScenarioCsv> dbCsv = apiScenarioFileService.getApiScenarioCsv(apiScenario.getId());
        List<CsvVariable> csvVariables = apiScenarioService.getCsvVariables(msScenario.getScenarioConfig());
        apiScenarioFileService.handleRefUpgradeFile(csvVariables, dbCsv);

        // 处理特殊的步骤详情
        ApiScenarioCopyStepMap apiScenarioCopyStepMap = apiScenarioService.addSpecialStepDetails(request.getSteps(), request.getStepDetails());
        // 处理copy的步骤文件
        apiScenarioFileService.handleRunCopyStepFiles(request, apiScenarioCopyStepMap, request.getStepDetails());

        ApiResourceRunRequest runRequest = new ApiResourceRunRequest();
        runRequest = setFileParam(request, runRequest);

        return executeRun(apiScenario, msScenario, request.getSteps(), request, runRequest, request.getReportId(), userId);
    }

    private ScenarioConfig getScenarioConfig(ApiScenarioDebugRequest request, boolean hasSave) {
        if (request.getScenarioConfig() != null) {
            // 优先使用前端传的配置
            return request.getScenarioConfig();
        } else if (hasSave) {
            // 没传并且保存过，则从数据库获取
            ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMapper.selectByPrimaryKey(request.getId());
            if (apiScenarioBlob != null) {
                return JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class);
            }
        }
        return new ScenarioConfig();
    }

    public ApiScenarioParseParam getApiScenarioParseParam(ApiScenarioDetail apiScenarioDetail) {
        ApiScenarioParseParam parseParam = new ApiScenarioParseParam();
        parseParam.setScenarioConfig(apiScenarioDetail.getScenarioConfig());
        parseParam.setStepDetails(Map.of());
        parseParam.setEnvironmentId(apiScenarioDetail.getEnvironmentId());
        parseParam.setGrouped(apiScenarioDetail.getGrouped());
        return parseParam;
    }

    /**
     * 执行时设置临时文件的相关参数
     *
     * @param request
     * @param runRequest
     */
    private ApiResourceRunRequest setFileParam(ApiScenarioDebugRequest request, ApiResourceRunRequest runRequest) {
        runRequest.getLinkFileIds().addAll(getLinkFileIds(request.getFileParam()));
        runRequest.getUploadFileIds().addAll(getUploadFileIds(request.getFileParam()));
        Map<String, ResourceAddFileParam> stepFileParam = request.getStepFileParam();
        if (MapUtils.isNotEmpty(stepFileParam)) {
            stepFileParam.values().forEach(fileParam -> {
                runRequest.getLinkFileIds().addAll(getLinkFileIds(fileParam));
                runRequest.getUploadFileIds().addAll(getUploadFileIds(fileParam));
            });
        }
        return runRequest;
    }

    public List<String> getLinkFileIds(ResourceAddFileParam fileParam) {
        if (fileParam != null && CollectionUtils.isNotEmpty(fileParam.getLinkFileIds())) {
            return fileParam.getLinkFileIds();
        }
        return List.of();
    }

    public List<String> getUploadFileIds(ResourceAddFileParam fileParam) {
        if (fileParam != null && CollectionUtils.isNotEmpty(fileParam.getUploadFileIds())) {
            return fileParam.getUploadFileIds();
        }
        return List.of();
    }

    public TaskRequestDTO executeRun(ApiScenario apiScenario,
                                     MsScenario msScenario,
                                     List<? extends ApiScenarioStepCommonDTO> steps,
                                     ApiScenarioParseParam parseParam,
                                     ApiResourceRunRequest runRequest,
                                     String reportId,
                                     String userId) {

        Project project = projectMapper.selectByPrimaryKey(apiScenario.getProjectId());

        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(1L);
        execTask.setTaskName(apiScenario.getName());
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.MANUAL.name());
        execTask.setTaskType(ExecTaskType.API_SCENARIO.name());

        ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(execTask.getId(), project.getId(), userId);
        execTaskItem.setOrganizationId(project.getOrganizationId());
        execTaskItem.setResourceType(ApiExecuteResourceType.API_SCENARIO.name());
        execTaskItem.setResourceId(apiScenario.getId());
        execTaskItem.setCaseId(apiScenario.getId());
        execTaskItem.setResourceName(apiScenario.getName());

        baseTaskHubService.insertExecTaskAndDetail(execTask, execTaskItem);

        msScenario.setResourceId(apiScenario.getId());

        // 解析生成场景树，并保存临时变量
        ApiScenarioParseTmpParam tmpParam = parse(msScenario, steps, parseParam);

        runRequest = setApiResourceRunRequestParam(msScenario, tmpParam, runRequest);

        String poolId = apiExecuteService.getProjectApiResourcePoolId(apiScenario.getProjectId());

        TaskRequestDTO taskRequest = getTaskRequest(reportId, apiScenario.getId(), apiScenario.getProjectId(), ApiExecuteRunMode.RUN.name());
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        TaskItem taskItem = taskRequest.getTaskItem();
        taskItem.setId(execTaskItem.getId());
        taskInfo.setTaskId(execTask.getId());
        taskInfo.getRunModeConfig().setPoolId(poolId);
        taskInfo.setSaveResult(true);
        taskInfo.setTriggerMode(TaskTriggerMode.MANUAL.name());
        taskInfo.getRunModeConfig().setEnvironmentId(parseParam.getEnvironmentId());
        taskRequest.getTaskItem().setRequestCount(tmpParam.getRequestCount().get());
        taskInfo.setUserId(userId);

        if (StringUtils.isEmpty(taskItem.getReportId())) {
            taskInfo.setRealTime(false);
        } else {
            // 如果传了报告ID，则实时获取结果
            taskInfo.setRealTime(true);
        }

        // 传了报告ID，则预生成报告
        ApiScenarioReport scenarioReport = getScenarioReport(apiScenario, userId);
        scenarioReport.setId(reportId);
        scenarioReport.setTriggerMode(TaskTriggerMode.MANUAL.name());
        scenarioReport.setRunMode(ApiBatchRunMode.PARALLEL.name());
        scenarioReport.setPoolId(poolId);
        scenarioReport.setEnvironmentId(parseParam.getEnvironmentId());
        scenarioReport.setWaitingTime(getGlobalWaitTime(parseParam.getScenarioConfig()));
        initApiScenarioReport(taskItem.getId(), apiScenario, scenarioReport);

        // 初始化报告步骤
        initScenarioReportSteps(steps, taskItem.getReportId());

        ApiScenarioParamConfig parseConfig = getApiScenarioParamConfig(apiScenario.getProjectId(), parseParam, tmpParam.getScenarioParseEnvInfo());
        parseConfig.setTaskItemId(taskItem.getId());
        return apiExecuteService.execute(runRequest, taskRequest, parseConfig);
    }

    public MsScenario getMsScenario(ApiScenarioDetail apiScenarioDetail) {
        MsScenario msScenario = new MsScenario();
        msScenario.setRefType(ApiScenarioStepRefType.DIRECT.name());
        msScenario.setScenarioConfig(apiScenarioDetail.getScenarioConfig());
        msScenario.setProjectId(apiScenarioDetail.getProjectId());
        return msScenario;
    }

    public ApiScenarioDetail getForRun(String scenarioId) {
        ApiScenarioDetail apiScenarioDetail = apiScenarioService.get(scenarioId);
        apiScenarioDetail.setSteps(filerDisableSteps(apiScenarioDetail.getSteps()));
        return apiScenarioDetail;
    }

    public ApiScenarioDetail getForRunWithTaskItemErrorMassage(String taskItemId, String scenarioId) {
        try {
            ApiScenarioDetail apiScenarioDetail = apiScenarioService.get(scenarioId);
            apiScenarioDetail.setSteps(filerDisableSteps(apiScenarioDetail.getSteps()));
            return apiScenarioDetail;
        } catch (MSException msException) {
            if (msException.getErrorCode().equals(ApiResultCode.CASE_NOT_EXIST)) {
                // 用例不存在记录任务项，错误信息
                apiCommonService.updateTaskItemErrorMassage(taskItemId, TaskItemErrorMessage.CASE_NOT_EXIST);
                throw new MSException(ApiResultCode.CASE_NOT_EXIST);
            }
            throw msException;
        }
    }

    /**
     * 过滤掉禁用的步骤
     */
    public List<ApiScenarioStepDTO> filerDisableSteps(List<ApiScenarioStepDTO> steps) {
        if (CollectionUtils.isEmpty(steps)) {
            return List.of();
        }
        return steps.stream()
                .filter(step -> {
                    boolean isEnable = BooleanUtils.isTrue(step.getEnable());
                    if (isEnable) {
                        step.setChildren(filerDisableSteps(step.getChildren()));
                    }
                    return isEnable;
                })
                .toList();
    }

    public GetRunScriptResult getRunScript(GetRunScriptRequest request, String id) {
        ApiScenarioDetail apiScenarioDetail = getForRun(id);
        return getRunScript(request, apiScenarioDetail);
    }

    public GetRunScriptResult getRunScript(GetRunScriptRequest request, ApiScenarioDetail apiScenarioDetail) {
        TaskItem taskItem = request.getTaskItem();
        ApiRunModeConfigDTO runModeConfig = request.getRunModeConfig();

        if (apiScenarioDetail == null) {
            if (runModeConfig.isIntegratedReport()) {
                // 用例不存在，则在执行集合中删除
                apiExecutionSetService.removeItem(request.getTaskId(), taskItem.getId());
            }
            LogUtils.info("当前执行任务的用例已删除 {}", taskItem.getId());
            return null;
        }

        String envId = getEnvId(runModeConfig, apiScenarioDetail.getEnvironmentId());
        boolean envGroup = getEnvGroup(runModeConfig, apiScenarioDetail.getGrouped());

        // 解析生成待执行的场景树
        MsScenario msScenario = getMsScenario(apiScenarioDetail);

        ApiScenarioParseParam parseParam = getApiScenarioParseParam(apiScenarioDetail);
        parseParam.setEnvironmentId(envId);
        parseParam.setGrouped(envGroup);

        GetRunScriptResult runScriptResult = new GetRunScriptResult();
        // 记录请求数量
        runScriptResult.setRequestCount(getRequestCount(apiScenarioDetail.getSteps()));

        msScenario.setResourceId(apiScenarioDetail.getId());
        ApiScenarioParseTmpParam tmpParam = parse(msScenario, apiScenarioDetail.getSteps(), parseParam);

        ApiResourceRunRequest runRequest = getApiResourceRunRequest(msScenario, tmpParam);

        ApiScenarioParamConfig parseConfig = getApiScenarioParamConfig(apiScenarioDetail.getProjectId(), parseParam, tmpParam.getScenarioParseEnvInfo());
        parseConfig.setTaskItemId(taskItem.getId());
        parseConfig.setRetryOnFail(request.getRunModeConfig().getRetryOnFail());
        parseConfig.setRetryConfig(request.getRunModeConfig().getRetryConfig());

        String script = apiExecuteService.parseExecuteScript(runRequest.getTestElement(), parseConfig);

        runScriptResult.setScript(script);

        apiExecuteService.setTaskItemFileParam(runRequest, taskItem);

        runScriptResult.setTaskResourceFile(taskItem.getTaskResourceFile());
        runScriptResult.setRefProjectResource(taskItem.getRefProjectResource());

        return runScriptResult;
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
    /**
     * 获取执行的环境ID
     * 优先使用运行配置的环境
     * 没有则使用用例自身的环境
     *
     * @return
     */
    public String getEnvId(ApiRunModeConfigDTO runModeConfig, String caseEnvId) {
        if (StringUtils.isBlank(runModeConfig.getEnvironmentId()) || StringUtils.equals(runModeConfig.getEnvironmentId(), CommonConstants.DEFAULT_NULL_VALUE)) {
            return caseEnvId;
        }
        return runModeConfig.getEnvironmentId();
    }

    public boolean getEnvGroup(ApiRunModeConfigDTO runModeConfig, boolean group) {
        if (StringUtils.isBlank(runModeConfig.getEnvironmentId()) || StringUtils.equals(runModeConfig.getEnvironmentId(), CommonConstants.DEFAULT_NULL_VALUE)) {
            return group;
        }
        return runModeConfig.getGrouped();
    }

    private void updateReportWaitTime(String reportId, ApiScenarioParseParam parseParam) {
        Long globalWaitTime = getGlobalWaitTime(parseParam.getScenarioConfig());
        if (globalWaitTime != null) {
            ApiScenarioReport apiScenarioReport = new ApiScenarioReport();
            apiScenarioReport.setId(reportId);
            apiScenarioReport.setWaitingTime(globalWaitTime);
            apiScenarioReportMapper.updateByPrimaryKeySelective(apiScenarioReport);
        }
    }

    public TaskRequestDTO debug(ApiScenarioDebugRequest request) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
        boolean hasSave = apiScenario != null;

        // 解析生成待执行的场景树
        MsScenario msScenario = new MsScenario();
        msScenario.setRefType(ApiScenarioStepRefType.DIRECT.name());
        msScenario.setScenarioConfig(getScenarioConfig(request, hasSave));
        msScenario.setProjectId(request.getProjectId());
        msScenario.setResourceId(request.getId());

        if (hasSave) {
            List<ApiScenarioCsv> dbCsv = apiScenarioFileService.getApiScenarioCsv(apiScenario.getId());
            List<CsvVariable> csvVariables = apiScenarioService.getCsvVariables(msScenario.getScenarioConfig());
            apiScenarioFileService.handleRefUpgradeFile(csvVariables, dbCsv);
        }

        // 处理特殊的步骤详情
        ApiScenarioCopyStepMap apiScenarioCopyStepMap = apiScenarioService.addSpecialStepDetails(request.getSteps(), request.getStepDetails());
        // 处理copy的步骤文件
        apiScenarioFileService.handleRunCopyStepFiles(request, apiScenarioCopyStepMap, request.getStepDetails());

        ApiScenarioParseTmpParam tmpParam = parse(msScenario, request.getSteps(), request);

        ApiResourceRunRequest runRequest = getApiResourceRunRequest(msScenario, tmpParam);

        runRequest = setFileParam(request, runRequest);

        TaskRequestDTO taskRequest = getTaskRequest(request.getReportId(), request.getId(), request.getProjectId(),
                apiExecuteService.getDebugRunModule(request.getFrontendDebug()));
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        taskInfo.setTaskId(request.getReportId());
        TaskItem taskItem = taskRequest.getTaskItem();
        taskItem.setId(request.getReportId());
        taskInfo.setSaveResult(false);
        taskInfo.setRealTime(true);
        taskItem.setRequestCount(tmpParam.getRequestCount().get());

        ApiScenarioParamConfig parseConfig = getApiScenarioParamConfig(request.getProjectId(), request, tmpParam.getScenarioParseEnvInfo());
        parseConfig.setTaskItemId(taskItem.getId());

        return apiExecuteService.execute(runRequest, taskRequest, parseConfig);
    }


    /**
     * 获取场景前置的总等待时间
     *
     * @param scenarioConfig
     * @return
     */
    public Long getGlobalWaitTime(ScenarioConfig scenarioConfig) {
        Long waitTime = null;
        if (scenarioConfig != null
                && scenarioConfig.getPreProcessorConfig() != null
                && scenarioConfig.getPreProcessorConfig().getProcessors() != null) {
            waitTime = 0L;
            for (MsProcessor processor : scenarioConfig
                    .getPreProcessorConfig()
                    .getProcessors()) {
                if (processor instanceof TimeWaitingProcessor timeWaitingProcessor
                        && timeWaitingProcessor.getEnable()
                        && timeWaitingProcessor.getDelay() != null) {
                    waitTime += timeWaitingProcessor.getDelay();
                }
            }
            waitTime = waitTime > 0 ? waitTime : null;
        }
        return waitTime;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String initApiScenarioReport(String taskItemId, ApiScenario apiScenario, GetRunScriptRequest request) {
        // 初始化报告
        ApiScenarioReport scenarioReport = getScenarioReport(apiScenario, request);
        scenarioReport.setStartTime(System.currentTimeMillis());
        apiScenarioReportService.insertApiScenarioReport(scenarioReport);
        return initApiScenarioReportDetail(taskItemId, apiScenario.getId(), scenarioReport.getId());
    }

    /**
     * 预生成用例的执行报告
     *
     * @param apiScenario
     * @return
     */
    public String initApiScenarioReport(String taskItemId, ApiScenario apiScenario, ApiScenarioReport scenarioReport) {
        // 初始化报告
        scenarioReport.setProjectId(apiScenario.getProjectId());
        apiScenarioReportService.insertApiScenarioReport(scenarioReport);
        return initApiScenarioReportDetail(taskItemId, apiScenario.getId(), scenarioReport.getId());
    }

    public String initApiScenarioReportDetail(String taskItemId, String apiScenarioId, String reportId) {
        // 创建报告和用例的关联关系
        ApiScenarioRecord scenarioRecord = getApiScenarioRecord(apiScenarioId, reportId);
        // 初始化报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = apiCommonService.getApiReportRelateTask(taskItemId, reportId);

        apiScenarioReportService.insertApiScenarioReportDetail(scenarioRecord, apiReportRelateTask);
        return scenarioRecord.getApiScenarioReportId();
    }

    public Long getRequestCount(List<ApiScenarioStepDTO> steps) {
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
     * 初始化场景报告步骤
     *
     * @param steps
     * @param reportId
     */
    public void initScenarioReportSteps(List<? extends ApiScenarioStepCommonDTO> steps, String reportId) {
        initScenarioReportSteps(null, steps, reportId);
    }

    public void initScenarioReportSteps(String parentId, List<? extends ApiScenarioStepCommonDTO> steps, String reportId) {
        List<ApiScenarioReportStep> scenarioReportSteps = getScenarioReportSteps(parentId, steps, reportId);
        apiScenarioReportService.insertApiScenarioReportStep(scenarioReportSteps);
    }

    /**
     * 获取场景报告步骤
     *
     * @param steps
     * @param reportId
     */
    public List<ApiScenarioReportStep> getScenarioReportSteps(String parentId, List<? extends ApiScenarioStepCommonDTO> steps, String reportId) {
        AtomicLong sort = new AtomicLong(1);
        List<ApiScenarioReportStep> scenarioReportSteps = new ArrayList<>();
        for (ApiScenarioStepCommonDTO step : steps) {
            if (StringUtils.isBlank(step.getUniqueId())) {
                // 如果没有步骤唯一ID，则生成唯一ID
                step.setUniqueId(IDGenerator.nextStr());
            }
            ApiScenarioReportStep scenarioReportStep = getScenarioReportStep(step, reportId, sort.getAndIncrement());
            scenarioReportStep.setParentId(parentId);
            scenarioReportSteps.add(scenarioReportStep);
            List<? extends ApiScenarioStepCommonDTO> children = step.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                scenarioReportSteps.addAll(getScenarioReportSteps(step.getUniqueId(), children, reportId));
            }
        }
        return scenarioReportSteps;
    }

    private ApiScenarioReportStep getScenarioReportStep(ApiScenarioStepCommonDTO step, String reportId, long sort) {
        ApiScenarioReportStep scenarioReportStep = new ApiScenarioReportStep();
        scenarioReportStep.setReportId(reportId);
        scenarioReportStep.setStepId(step.getUniqueId());
        scenarioReportStep.setSort(sort);
        scenarioReportStep.setName(step.getName());
        scenarioReportStep.setStepType(step.getStepType());
        return scenarioReportStep;
    }

    public ApiScenarioRecord getApiScenarioRecord(String apiScenarioId, String reportId) {
        ApiScenarioRecord scenarioRecord = new ApiScenarioRecord();
        scenarioRecord.setApiScenarioId(apiScenarioId);
        scenarioRecord.setApiScenarioReportId(reportId);
        return scenarioRecord;
    }

    public ApiScenarioReport getScenarioReport(ApiScenario apiScenario, String userId) {
        ApiScenarioReport scenarioReport = getScenarioReport(userId);
        scenarioReport.setName(apiScenario.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        scenarioReport.setEnvironmentId(apiScenario.getEnvironmentId());
        scenarioReport.setProjectId(apiScenario.getProjectId());
        return scenarioReport;
    }

    public ApiScenarioReport getScenarioReport(String userId) {
        ApiScenarioReport scenarioReport = new ApiScenarioReport();
        scenarioReport.setId(IDGenerator.nextStr());
        scenarioReport.setDeleted(false);
        scenarioReport.setIntegrated(false);
        scenarioReport.setExecStatus(ExecStatus.PENDING.name());
        scenarioReport.setStartTime(System.currentTimeMillis());
        scenarioReport.setUpdateTime(System.currentTimeMillis());
        scenarioReport.setUpdateUser(userId);
        scenarioReport.setCreateUser(userId);
        return scenarioReport;
    }

    public ApiScenarioReport getScenarioReport(ApiScenario apiScenario, GetRunScriptRequest request) {
        ApiScenarioReport scenarioReport = getScenarioReport(apiScenario, request.getUserId());
        scenarioReport.setExecStatus(ExecStatus.RUNNING.name());
        scenarioReport.setRunMode(request.getRunMode());
        scenarioReport.setTriggerMode(request.getTriggerMode());
        scenarioReport.setPoolId(request.getPoolId());
        if (StringUtils.isNotBlank(request.getTaskItem().getReportId())) {
            scenarioReport.setId(request.getTaskItem().getReportId());
        }
        scenarioReport.setEnvironmentId(getEnvId(request.getRunModeConfig(), apiScenario.getEnvironmentId()));
        return scenarioReport;
    }

    public String getEnvId(GetRunScriptRequest request, String caseEnvId) {
        ApiRunModeConfigDTO runModeConfig = request.getRunModeConfig();
        if (StringUtils.isBlank(runModeConfig.getEnvironmentId()) || StringUtils.equals(runModeConfig.getEnvironmentId(), CommonConstants.DEFAULT_NULL_VALUE)) {
            return caseEnvId;
        }
        return runModeConfig.getEnvironmentId();
    }

    public ApiScenarioParamConfig getApiScenarioParamConfig(String projectId, ApiScenarioParseParam request, ApiScenarioParseEnvInfo scenarioParseEnvInfo) {
        ApiScenarioParamConfig parseConfig = new ApiScenarioParamConfig();
        parseConfig.setTestElementClassPluginIdMap(apiPluginService.getTestElementPluginMap());
        parseConfig.setTestElementClassProtocolMap(apiPluginService.getTestElementProtocolMap());
        parseConfig.setGrouped(request.getGrouped());
        parseConfig.setRootScenarioConfig(request.getScenarioConfig());
        if (BooleanUtils.isTrue(request.getGrouped())) {
            // 设置环境组 map
            parseConfig.setProjectEnvMap(getProjectEnvMap(scenarioParseEnvInfo, request.getEnvironmentId()));
        } else {
            // 设置环境
            parseConfig.setEnvConfig(scenarioParseEnvInfo.getEnvMap().get(request.getEnvironmentId()));
        }
        // 设置全局参数，接口调试不使用全局参数
        parseConfig.setGlobalParams(apiExecuteService.getGlobalParam(projectId));
        return parseConfig;
    }

    public ApiResourceRunRequest getApiResourceRunRequest(MsScenario msScenario, ApiScenarioParseTmpParam tmpParam) {
        ApiResourceRunRequest runRequest = new ApiResourceRunRequest();
        return setApiResourceRunRequestParam(msScenario, tmpParam, runRequest);
    }

    private ApiResourceRunRequest setApiResourceRunRequestParam(MsScenario msScenario, ApiScenarioParseTmpParam tmpParam, ApiResourceRunRequest runRequest) {
        runRequest.setFileResourceIds(tmpParam.getFileResourceIds());
        runRequest.setFileStepScenarioMap(tmpParam.getFileStepScenarioMap());
        runRequest.setRefProjectIds(tmpParam.getRefProjectIds());
        runRequest.setTestElement(msScenario);
        return runRequest;
    }

    /**
     * 将步骤转换成场景树
     * 并保存临时变量
     *
     * @param msScenario
     * @param steps
     * @param parseParam
     * @return
     */
    public ApiScenarioParseTmpParam parse(MsScenario msScenario,
                                          List<? extends ApiScenarioStepCommonDTO> steps,
                                          ApiScenarioParseParam parseParam) {
        // 记录引用的资源ID
        Map<String, List<String>> refResourceMap = new HashMap<>();
        buildRefResourceIdMap(steps, refResourceMap);

        ApiScenarioParseTmpParam tmpParam = new ApiScenarioParseTmpParam();

        // 查询引用的资源详情
        tmpParam.setResourceDetailMap(getResourceDetailMap(refResourceMap));

        // 查询复制的步骤详情
        tmpParam.setStepDetailMap(getStepDetailMap(steps, parseParam.getStepDetails()));

        // 获取场景环境相关配置
        tmpParam.setScenarioParseEnvInfo(getScenarioParseEnvInfo(refResourceMap, parseParam.getEnvironmentId(), parseParam.getGrouped()));
        parseStep2MsElement(msScenario, steps, tmpParam, msScenario.getResourceId());

        // 设置 HttpElement 的模块信息
        setApiDefinitionExecuteInfo(tmpParam.getUniqueIdStepMap(), tmpParam.getStepTypeHttpElementMap());

        // 设置使用脚本前后置的公共脚本信息
        apiCommonService.setCommonElementEnableCommonScriptInfo(tmpParam.getCommonElements());
        apiCommonService.setScriptElementEnableCommonScriptInfo(tmpParam.getScriptElements());

        return tmpParam;
    }

    public ApiScenarioParseTmpParam parse(MsScenario msScenario,
                                          Map<String, ApiDefinitionBlob> apiBlobMap,
                                          Map<String, ApiTestCaseBlob> apiTestCaseBlobMap,
                                          Map<String, ApiScenarioBlob> scenarioBlobMap,
                                          List<? extends ApiScenarioStepCommonDTO> steps,
                                          ApiScenarioParseParam parseParam) {
        ApiScenarioParseTmpParam tmpParam = new ApiScenarioParseTmpParam();

        Map<String, List<String>> refResourceMap = new HashMap<>();
        buildRefResourceIdMap(steps, refResourceMap);
        Map<String, String> resourceBlobMap = new HashMap<>();
        List<String> apiIds = refResourceMap.get(ApiScenarioStepType.API.name());
        if (CollectionUtils.isNotEmpty(apiIds)) {
            apiIds.forEach(apiId -> {
                ApiDefinitionBlob blob = apiBlobMap.get(apiId);
                if (blob != null) {
                    resourceBlobMap.put(blob.getId(), new String(blob.getRequest()));
                }
            });
        }
        List<String> apiCaseIds = refResourceMap.get(ApiScenarioStepType.API_CASE.name());
        if (CollectionUtils.isNotEmpty(apiCaseIds)) {
            apiCaseIds.forEach(apiCaseId -> {
                ApiTestCaseBlob blob = apiTestCaseBlobMap.get(apiCaseId);
                if (blob != null) {
                    resourceBlobMap.put(blob.getId(), new String(blob.getRequest()));
                }
            });
        }
        List<String> apiScenarioIds = refResourceMap.get(ApiScenarioStepType.API_SCENARIO.name());
        if (CollectionUtils.isNotEmpty(apiScenarioIds)) {
            apiScenarioIds.forEach(apiScenarioId -> {
                ApiScenarioBlob blob = scenarioBlobMap.get(apiScenarioId);
                if (blob != null) {
                    resourceBlobMap.put(blob.getId(), new String(blob.getConfig()));
                }
            });
        }
        tmpParam.setResourceDetailMap(resourceBlobMap);
        // 查询复制的步骤详情
        tmpParam.setStepDetailMap(getStepDetailMap(steps, parseParam.getStepDetails()));
        // 获取场景环境相关配置
        tmpParam.setScenarioParseEnvInfo(getScenarioParseEnvInfo(refResourceMap, parseParam.getEnvironmentId(), parseParam.getGrouped()));
        parseStep2MsElement(msScenario, steps, tmpParam, msScenario.getResourceId());
        // 设置 HttpElement 的模块信息
        setApiDefinitionExecuteInfo(tmpParam.getUniqueIdStepMap(), tmpParam.getStepTypeHttpElementMap());
        // 设置使用脚本前后置的公共脚本信息
        apiCommonService.setCommonElementEnableCommonScriptInfo(tmpParam.getCommonElements());
        apiCommonService.setScriptElementEnableCommonScriptInfo(tmpParam.getScriptElements());

        return tmpParam;
    }

    private void buildRefResourceIdMap(List<? extends ApiScenarioStepCommonDTO> steps, Map<String, List<String>> refResourceIdMap) {
        for (ApiScenarioStepCommonDTO step : steps) {
            if (apiScenarioService.isRefOrPartialRef(step.getRefType()) && BooleanUtils.isTrue(step.getEnable())) {
                // 记录引用的步骤ID
                List<String> resourceIds = refResourceIdMap.computeIfAbsent(step.getStepType(), k -> new ArrayList<>());
                resourceIds.add(step.getResourceId());
            }

            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                buildRefResourceIdMap(step.getChildren(), refResourceIdMap);
            }
        }
    }

    private Map<String, String> getStepDetailMap(List<? extends ApiScenarioStepCommonDTO> steps, Map<String, Object> stepDetailsParam) {
        List<String> needBlobStepIds = getHasDetailStepIds(steps, stepDetailsParam);
        Map<String, String> stepDetails = apiScenarioService.getStepBlobByIds(needBlobStepIds).stream()
                .collect(Collectors.toMap(ApiScenarioStepBlob::getId, blob -> new String(blob.getContent())));
        // 前端有传，就用前端传的
        if (stepDetailsParam != null) {
            stepDetailsParam.forEach((stepId, detail) -> stepDetails.put(stepId, detail instanceof byte[] bytes ? new String(bytes) : JSON.toJSONString(detail)));
        }
        return stepDetails;
    }

    private List<String> getHasDetailStepIds(List<? extends ApiScenarioStepCommonDTO> steps, Map<String, Object> stepDetailsParam) {
        List<String> needBlobStepIds = new ArrayList<>();
        for (ApiScenarioStepCommonDTO step : steps) {
            List<? extends ApiScenarioStepCommonDTO> children = step.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                needBlobStepIds.addAll(getHasDetailStepIds(children, stepDetailsParam));
            }
            if (BooleanUtils.isFalse(step.getEnable())) {
                continue;
            }
            if (!hasStepDetail(step.getStepType())) {
                continue;
            }
            if (stepDetailsParam != null && stepDetailsParam.containsKey(step.getId())) {
                // 前端传了blob，不需要再查
                continue;
            }
            needBlobStepIds.add(step.getId());
        }
        return needBlobStepIds;
    }


    /**
     * 非完全引用的步骤和接口定义的步骤，才需要查 blob
     *
     * @param stepType
     * @return
     */
    private boolean hasStepDetail(String stepType) {
        return !StringUtils.equals(stepType, ApiScenarioStepRefType.REF.name())
                || apiScenarioService.isApi(stepType);
    }

    private Map<String, String> getResourceDetailMap(Map<String, List<String>> refResourceMap) {
        Map<String, String> resourceBlobMap = new HashMap<>();
        List<String> apiIds = refResourceMap.get(ApiScenarioStepType.API.name());
        List<ApiDefinitionBlob> apiDefinitionBlobs = apiDefinitionService.getBlobByIds(apiIds);
        apiDefinitionBlobs.forEach(blob -> resourceBlobMap.put(blob.getId(), new String(blob.getRequest())));

        List<String> apiCaseIds = refResourceMap.get(ApiScenarioStepType.API_CASE.name());
        List<ApiTestCaseBlob> apiTestCaseBlobs = apiTestCaseService.getBlobByIds(apiCaseIds);
        apiTestCaseBlobs.forEach(blob -> resourceBlobMap.put(blob.getId(), new String(blob.getRequest())));

        List<String> apiScenarioIds = refResourceMap.get(ApiScenarioStepType.API_SCENARIO.name());
        List<ApiScenarioBlob> apiScenarioBlobs = getBlobByIds(apiScenarioIds);
        apiScenarioBlobs.forEach(blob -> resourceBlobMap.put(blob.getId(), new String(blob.getConfig())));
        return resourceBlobMap;
    }

    private List<ApiScenarioBlob> getBlobByIds(List<String> apiScenarioIds) {
        if (CollectionUtils.isEmpty(apiScenarioIds)) {
            return Collections.emptyList();
        }
        ApiScenarioBlobExample example = new ApiScenarioBlobExample();
        example.createCriteria().andIdIn(apiScenarioIds);
        return apiScenarioBlobMapper.selectByExampleWithBLOBs(example);
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId, String runModule) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId, runModule);
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, resourceId);
        taskRequest.setTaskInfo(taskInfo);
        taskRequest.setTaskItem(taskItem);
        return taskRequest;
    }

    public TaskInfo getTaskInfo(String projectId, String runModule) {
        TaskInfo taskInfo = apiExecuteService.getTaskInfo(projectId);
        taskInfo.setResourceType(ApiResourceType.API_SCENARIO.name());
        taskInfo.setRunMode(runModule);
        return taskInfo;
    }

    /**
     * 设置 HttpElement 的模块信息
     * 用户环境中的模块过滤
     *
     * @param uniqueIdStepMap
     * @param stepTypeHttpElementMap
     */
    private void setApiDefinitionExecuteInfo(Map<String, ApiScenarioStepCommonDTO> uniqueIdStepMap, Map<String, List<MsHTTPElement>> stepTypeHttpElementMap) {
        setApiDefinitionExecuteInfo(uniqueIdStepMap, stepTypeHttpElementMap.get(ApiScenarioStepType.API.name()), apiDefinitionService::getModuleInfoByIds);
        setApiDefinitionExecuteInfo(uniqueIdStepMap, stepTypeHttpElementMap.get(ApiScenarioStepType.API_CASE.name()), apiTestCaseService::getModuleInfoByIds);
    }

    /**
     * 设置 MsHTTPElement 中的 method 等信息
     *
     * @param httpElements
     * @param getDefinitionInfoFunc
     */
    public void setApiDefinitionExecuteInfo(Map<String, ApiScenarioStepCommonDTO> uniqueIdStepMap, List<MsHTTPElement> httpElements, Function<List<String>, List<ApiDefinitionExecuteInfo>> getDefinitionInfoFunc) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(httpElements)) {
            List<String> resourceIds = httpElements.stream().map(MsHTTPElement::getResourceId).collect(Collectors.toList());
            // 获取接口模块信息
            Map<String, ApiDefinitionExecuteInfo> resourceModuleMap = apiCommonService.getApiDefinitionExecuteInfoMap(getDefinitionInfoFunc, resourceIds);
            httpElements.forEach(httpElement -> {
                ApiDefinitionExecuteInfo definitionExecuteInfo = resourceModuleMap.get(httpElement.getResourceId());
                String path = httpElement.getPath();
                String method = httpElement.getMethod();

                // httpElement 设置模块,请求方法等信息
                apiCommonService.setApiDefinitionExecuteInfo(httpElement, definitionExecuteInfo);

                ApiScenarioStepCommonDTO step = uniqueIdStepMap.get(httpElement.getStepId());
                if (step != null && apiScenarioService.isCopyApi(step.getStepType(), step.getRefType())) {
                    // 复制的接口定义，不使用源接口定义的path和method
                    httpElement.setPath(path);
                    httpElement.setMethod(method);
                }
            });
        }
    }

    /**
     * 设置脚本解析-环境相关参数
     */
    private ApiScenarioParseEnvInfo getScenarioParseEnvInfo(Map<String, List<String>> refResourceMap, String currentEnvId, Boolean isCurrentEnvGrouped) {
        List<String> apiScenarioIds = refResourceMap.get(ApiScenarioStepType.API_SCENARIO.name());
        List<String> envIds = new ArrayList<>();
        List<String> envGroupIds = new ArrayList<>();
        ApiScenarioParseEnvInfo envInfo = new ApiScenarioParseEnvInfo();

        if (BooleanUtils.isTrue(isCurrentEnvGrouped)) {
            envGroupIds.add(currentEnvId);
        } else {
            envIds.add(currentEnvId);
        }

        if (CollectionUtils.isNotEmpty(apiScenarioIds)) {
            Map<String, EnvironmentModeDTO> refScenarioEnvMap = new HashMap<>();
            List<ApiScenario> apiScenarios = getApiScenarioByIds(apiScenarioIds);
            for (ApiScenario scenario : apiScenarios) {
                EnvironmentModeDTO envMode = new EnvironmentModeDTO();
                envMode.setEnvironmentId(scenario.getEnvironmentId());
                envMode.setGrouped(scenario.getGrouped());
                if (BooleanUtils.isTrue(scenario.getGrouped())) {
                    // 记录环境组ID
                    envGroupIds.add(scenario.getEnvironmentId());
                } else {
                    // 记录环境ID
                    envIds.add(scenario.getEnvironmentId());
                }
                // 保存场景的环境配置信息
                refScenarioEnvMap.put(scenario.getId(), envMode);
            }
            envInfo.setRefScenarioEnvMap(refScenarioEnvMap);
        }

        // 查询环境组中的环境ID列表
        Map<String, List<String>> envGroupMap = new HashMap<>();
        environmentGroupService.getEnvironmentGroupRelations(envGroupIds).forEach(environmentGroupRelation -> {
            envGroupMap.putIfAbsent(environmentGroupRelation.getEnvironmentGroupId(), new ArrayList<>());
            envGroupMap.get(environmentGroupRelation.getEnvironmentGroupId()).add(environmentGroupRelation.getEnvironmentId());
            envIds.add(environmentGroupRelation.getEnvironmentId());
        });

        // 获取环境的配置信息
        List<String> distinctEnvIds = envIds.stream().distinct().toList();
        Map<String, EnvironmentInfoDTO> envMap = environmentService.getByIds(distinctEnvIds)
                .stream()
                .collect(Collectors.toMap(EnvironmentInfoDTO::getId, Function.identity()));

        envInfo.setEnvGroupMap(envGroupMap);
        envInfo.setEnvMap(envMap);

        envMap.forEach((envId, envInfoDTO) -> apiExecuteService.handleHttpModuleMatchRule(envInfoDTO));

        return envInfo;
    }

    private List<ApiScenario> getApiScenarioByIds(List<String> apiScenarioIds) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(apiScenarioIds);
        return apiScenarioMapper.selectByExample(example);
    }

    /**
     * 将步骤解析成 MsTestElement 树结构
     */
    private void parseStep2MsElement(AbstractMsTestElement parentElement,
                                     List<? extends ApiScenarioStepCommonDTO> steps,
                                     ApiScenarioParseTmpParam parseParam,
                                     String scenarioId) {
        if (CollectionUtils.isNotEmpty(steps)) {
            parentElement.setChildren(new LinkedList<>());
        }

        Map<String, String> stepDetailMap = parseParam.getStepDetailMap();
        Map<String, String> resourceDetailMap = parseParam.getResourceDetailMap();
        Map<String, List<MsHTTPElement>> stepTypeHttpElementMap = parseParam.getStepTypeHttpElementMap();
        for (ApiScenarioStepCommonDTO step : steps) {
            StepParser stepParser = StepParserFactory.getStepParser(step.getStepType());
            if (BooleanUtils.isFalse(step.getEnable())) {
                continue;
            }
            apiScenarioService.setPartialRefStepEnable(step, stepDetailMap);

            if (apiScenarioService.isRequestStep(step) && BooleanUtils.isTrue(step.getEnable())) {
                // 记录待执行的请求总数
                parseParam.getRequestCount().getAndIncrement();
            }

            if (StringUtils.isBlank(step.getUniqueId())) {
                // 如果调试的时候前端没有传步骤唯一ID，则生成唯一ID
                step.setUniqueId(IDGenerator.nextStr());
            }

            parseParam.getUniqueIdStepMap().put(step.getUniqueId(), step);

            // 将步骤详情解析生成对应的MsTestElement
            AbstractMsTestElement msTestElement = stepParser.parseTestElement(step,
                    MapUtils.isNotEmpty(resourceDetailMap) ? resourceDetailMap.getOrDefault(step.getResourceId(), StringUtils.EMPTY) : StringUtils.EMPTY, stepDetailMap.get(step.getId()));
            if (msTestElement != null) {
                if (msTestElement instanceof MsHTTPElement msHTTPElement) {
                    // 暂存http类型的步骤
                    stepTypeHttpElementMap.putIfAbsent(step.getStepType(), new LinkedList<>());
                    stepTypeHttpElementMap.get(step.getStepType()).add(msHTTPElement);
                } else if (msTestElement instanceof MsScriptElement msScriptElement) {
                    parseParam.getScriptElements().add(msScriptElement);
                }
                msTestElement.setProjectId(step.getProjectId());
                msTestElement.setResourceId(step.getResourceId());
                msTestElement.setName(step.getName());
                // 步骤ID，设置为唯一ID
                msTestElement.setStepId(step.getUniqueId());
                msTestElement.setCsvIds(step.getCsvIds());

                // 记录引用的资源ID和项目ID，下载执行文件时需要使用
                parseParam.getRefProjectIds().add(step.getProjectId());
                if (apiScenarioService.isRefOrPartialRef(step.getRefType()) && !apiScenarioService.isRefApi(step.getStepType(), step.getRefType())) {
                    // 引用的步骤记录引用的资源ID
                    parseParam.getFileResourceIds().add(step.getResourceId());
                } else if (msTestElement instanceof MsHTTPElement) {
                    // 非引用的步骤记录步骤ID
                    parseParam.getFileResourceIds().add(step.getId());
                    parseParam.getFileStepScenarioMap().put(step.getId(), scenarioId);
                }

                // 设置环境等，运行时场景参数
                setMsScenarioParam(parseParam.getScenarioParseEnvInfo(), step, msTestElement);

                // 记录 msCommonElement
                Optional.ofNullable(apiCommonService.getMsCommonElement(msTestElement))
                        .ifPresent(msCommonElement -> parseParam.getCommonElements().add(msCommonElement));
                // 组装树结构
                parentElement.getChildren().add(msTestElement);

                if (CollectionUtils.isNotEmpty(step.getChildren())) {
                    if (apiScenarioService.isScenarioStep(step.getStepType()) && apiScenarioService.isRefOrPartialRef(step.getRefType())) {
                        scenarioId = step.getResourceId();
                    }
                    parseStep2MsElement(msTestElement, step.getChildren(), parseParam, scenarioId);
                }
            }
        }
    }

    /**
     * 设置运行时场景参数
     *
     * @param scenarioParseEnvInfo
     * @param step
     * @param msTestElement
     */
    private void setMsScenarioParam(ApiScenarioParseEnvInfo scenarioParseEnvInfo,
                                    ApiScenarioStepCommonDTO step,
                                    AbstractMsTestElement msTestElement) {
        // 引用的场景设置场景参数
        if (!apiScenarioService.isScenarioStep(step.getStepType()) || !apiScenarioService.isRefOrPartialRef(step.getRefType())
                || !(msTestElement instanceof MsScenario msScenario)) {
            return;
        }

        if (step.getConfig() != null) {
            // 设置场景步骤的运行参数
            msScenario.setScenarioStepConfig(JSON.parseObject(JSON.toJSONString(step.getConfig()), ScenarioStepConfig.class));
        }

        // 获取当前场景配置的环境信息
        EnvironmentModeDTO environmentModeDTO = scenarioParseEnvInfo.getRefScenarioEnvMap().get(step.getResourceId());

        if (environmentModeDTO != null) {
            String environmentId = environmentModeDTO.getEnvironmentId();

            // 设置是否是环境组
            Boolean isGrouped = environmentModeDTO.getGrouped();
            msScenario.setGrouped(isGrouped);
            Map<String, EnvironmentInfoDTO> envMap = scenarioParseEnvInfo.getEnvMap();

            if (BooleanUtils.isTrue(isGrouped)) {
                // 设置环境组 map
                msScenario.setProjectEnvMap(getProjectEnvMap(scenarioParseEnvInfo, environmentId));
            } else {
                // 设置环境
                msScenario.setEnvironmentInfo(envMap.get(environmentId));
            }
        }
    }

    /**
     * 从 scenarioParseEnvInfo 获取对应环境组的 projectEnvMap
     *
     * @param scenarioParseEnvInfo 环境信息
     * @param environmentId        环境ID
     * @return projectEnvMap
     */
    private Map<String, EnvironmentInfoDTO> getProjectEnvMap(ApiScenarioParseEnvInfo scenarioParseEnvInfo, String environmentId) {
        Map<String, List<String>> envGroupMap = scenarioParseEnvInfo.getEnvGroupMap();
        List<String> envIds = envGroupMap.get(environmentId);
        Map<String, EnvironmentInfoDTO> projectEnvMap = new HashMap<>();
        for (String envId : envIds) {
            EnvironmentInfoDTO environmentInfoDTO = scenarioParseEnvInfo.getEnvMap().get(envId);
            projectEnvMap.put(environmentInfoDTO.getProjectId(), environmentInfoDTO);
        }
        return projectEnvMap;
    }

    public void runRun(ExecTask execTask, ExecTaskItem execTaskItem, String userId) {
        ApiScenario apiScenario = apiScenarioService.checkResourceIsNoDeleted(execTaskItem.getResourceId());

        String poolId = apiExecuteService.getProjectApiResourcePoolId(apiScenario.getProjectId());

        TaskRequestDTO taskRequest = getTaskRequest(null, apiScenario.getId(), apiScenario.getProjectId(), ApiExecuteRunMode.RUN.name());
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        TaskItem taskItem = taskRequest.getTaskItem();
        taskItem.setId(execTaskItem.getId());
        taskInfo.setTaskId(execTask.getId());
        taskInfo.getRunModeConfig().setPoolId(poolId);
        taskInfo.setSaveResult(true);
        taskInfo.setTriggerMode(TaskTriggerMode.MANUAL.name());
        taskInfo.setUserId(userId);
        taskInfo.setRealTime(false);

        apiExecuteService.execute(taskRequest);
    }

    public void deleteStepsByReportId(String reportId) {
        ApiScenarioReportStepExample example = new ApiScenarioReportStepExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        apiScenarioReportStepMapper.deleteByExample(example);
    }
}
