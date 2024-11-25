package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiDefinitionExecuteInfo;
import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.definition.ApiCaseRunRequest;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiTestCaseBlobMapper;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseRunService {
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private EnvironmentService environmentService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private BaseTaskHubService baseTaskHubService;

    /**
     * 接口执行
     * 传请求详情执行
     *
     * @param request
     * @return
     */
    public TaskRequestDTO run(ApiCaseRunRequest request, String userId) {
        ApiTestCase apiTestCase = apiTestCaseService.checkResourceExist(request.getId());
        ApiResourceRunRequest runRequest = apiExecuteService.getApiResourceRunRequest(request);
        apiTestCase.setEnvironmentId(request.getEnvironmentId());
        return executeRun(runRequest, apiTestCase, request.getReportId(), userId);
    }

    /**
     * 接口执行
     * 传ID执行
     *
     * @param id
     * @param reportId
     * @param userId
     * @return
     */
    public TaskRequestDTO run(String id, String reportId, String userId) {
        ApiTestCase apiTestCase = apiTestCaseService.checkResourceExist(id);
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(id);

        ApiResourceRunRequest runRequest = new ApiResourceRunRequest();
        runRequest.setTestElement(apiTestCaseService.getTestElement(apiTestCaseBlob));

        return executeRun(runRequest, apiTestCase, reportId, userId);
    }

    /**
     * 任务重跑
     *
     * @param userId
     * @return
     */
    public TaskRequestDTO runRun(ExecTask execTask, ExecTaskItem execTaskItem, String userId) {
        String id = execTaskItem.getResourceId();
        ApiTestCase apiTestCase = apiTestCaseService.checkResourceExist(id);
        String poolId = apiExecuteService.getProjectApiResourcePoolId(apiTestCase.getProjectId());

        TaskRequestDTO taskRequest = getTaskRequest(null, id, apiTestCase.getProjectId(), ApiExecuteRunMode.RUN.name());
        TaskItem taskItem = taskRequest.getTaskItem();
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        taskInfo.getRunModeConfig().setPoolId(poolId);
        taskInfo.setSaveResult(true);
        taskInfo.setUserId(userId);
        taskInfo.setTaskId(execTask.getId());
        taskInfo.setNeedParseScript(true);
        taskInfo.setRerun(true);
        taskItem.setId(execTaskItem.getId());

        return apiExecuteService.execute(taskRequest);
    }

    /**
     * 接口执行
     * 保存报告
     *
     * @param runRequest
     * @param apiTestCase
     * @param reportId
     * @param userId
     * @return
     */
    public TaskRequestDTO executeRun(ApiResourceRunRequest runRequest, ApiTestCase apiTestCase, String reportId, String userId) {
        String poolId = apiExecuteService.getProjectApiResourcePoolId(apiTestCase.getProjectId());
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());

        ExecTask execTask = newExecTask(apiTestCase, userId, project);
        ExecTaskItem execTaskItem = newExecTaskItem(apiTestCase, userId, project, execTask.getId());
        baseTaskHubService.insertExecTaskAndDetail(execTask, execTaskItem);

        TaskRequestDTO taskRequest = getTaskRequest(reportId, apiTestCase.getId(), apiTestCase.getProjectId(), ApiExecuteRunMode.RUN.name());
        TaskItem taskItem = taskRequest.getTaskItem();
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        taskInfo.getRunModeConfig().setPoolId(poolId);
        taskInfo.setSaveResult(true);
        taskInfo.setUserId(userId);
        taskInfo.setTaskId(execTask.getId());
        taskItem.setId(execTaskItem.getId());

        if (StringUtils.isEmpty(taskItem.getReportId())) {
            taskInfo.setRealTime(false);
        } else {
            // 如果传了报告ID，则实时获取结果
            taskInfo.setRealTime(true);
            taskItem.setReportId(reportId);
        }

        return doExecute(taskRequest, runRequest, apiTestCase.getApiDefinitionId(), apiTestCase.getEnvironmentId());
    }

    private ExecTaskItem newExecTaskItem(ApiTestCase apiTestCase, String userId, Project project, String taskId) {
        ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(taskId, project.getId(), userId);
        execTaskItem.setOrganizationId(project.getOrganizationId());
        execTaskItem.setResourceType(ApiExecuteResourceType.API_CASE.name());
        execTaskItem.setResourceId(apiTestCase.getId());
        execTaskItem.setCaseId(apiTestCase.getId());
        execTaskItem.setResourceName(apiTestCase.getName());
        return execTaskItem;
    }

    private ExecTask newExecTask(ApiTestCase apiTestCase, String userId, Project project) {
        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(1L);
        execTask.setTaskName(apiTestCase.getName());
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.MANUAL.name());
        execTask.setTaskType(ExecTaskType.API_CASE.name());
        return execTask;
    }

    /**
     * 接口调试
     * 不存报告，实时获取结果
     *
     * @param request
     * @return
     */
    public TaskRequestDTO debug(ApiCaseRunRequest request, String userId) {
        TaskRequestDTO taskRequest = getTaskRequest(request.getReportId(), request.getId(),
                request.getProjectId(), apiExecuteService.getDebugRunModule(request.getFrontendDebug()));
        taskRequest.getTaskInfo().setTaskId(UUID.randomUUID().toString());
        taskRequest.getTaskInfo().setSaveResult(false);
        taskRequest.getTaskInfo().setRealTime(true);
        taskRequest.getTaskInfo().setUserId(userId);
        taskRequest.getTaskItem().setId(UUID.randomUUID().toString());

        ApiResourceRunRequest runRequest = apiExecuteService.getApiResourceRunRequest(request);

        return doExecute(taskRequest, runRequest, request.getApiDefinitionId(), request.getEnvironmentId());
    }

    public TaskRequestDTO doExecute(TaskRequestDTO taskRequest, ApiResourceRunRequest runRequest, String apiDefinitionId, String envId) {

        ApiParamConfig apiParamConfig = apiExecuteService.getApiParamConfig(taskRequest.getTaskInfo().getProjectId());

        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionId);

        // 设置环境
        apiParamConfig.setEnvConfig(environmentService.get(envId));

        taskRequest.getTaskInfo().getRunModeConfig().setEnvironmentId(envId);
        apiParamConfig.setTaskItemId(taskRequest.getTaskItem().getId());
        // 设置 method 等信息
        apiCommonService.setApiDefinitionExecuteInfo(runRequest.getTestElement(), apiDefinition);

        return apiExecuteService.apiExecute(runRequest, taskRequest, apiParamConfig);
    }

    public GetRunScriptResult getRunScript(GetRunScriptRequest request, ApiTestCase apiTestCase) {
        TaskItem taskItem = request.getTaskItem();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiTestCase.getApiDefinitionId());
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(apiTestCase.getId());
        ApiParamConfig apiParamConfig = apiExecuteService.getApiParamConfig(apiTestCase.getProjectId());
        apiParamConfig.setRetryOnFail(request.getRunModeConfig().getRetryOnFail());
        apiParamConfig.setRetryConfig(request.getRunModeConfig().getRetryConfig());

        AbstractMsTestElement msTestElement = apiTestCaseService.getTestElement(apiTestCaseBlob);
        // 设置 method 等信息
        apiCommonService.setApiDefinitionExecuteInfo(msTestElement, BeanUtils.copyBean(new ApiDefinitionExecuteInfo(), apiDefinition));

        apiExecuteService.setTestElementParam(msTestElement, apiTestCase.getProjectId(), request.getTaskItem());
        // 处理模块匹配
        apiExecuteService.handleHttpModuleMatchRule(apiParamConfig.getEnvConfig());

        // 设置环境信息
        apiParamConfig.setEnvConfig(environmentService.get(getEnvId(request.getRunModeConfig(), apiTestCase.getEnvironmentId())));
        GetRunScriptResult runScriptResult = new GetRunScriptResult();
        // 记录请求数量
        runScriptResult.setRequestCount(1L);
        runScriptResult.setScript(apiExecuteService.parseExecuteScript(msTestElement, apiParamConfig));

        // 设置资源关联的文件信息
        apiExecuteService.setTaskItemFileParam(taskItem);
        runScriptResult.setTaskResourceFile(taskItem.getTaskResourceFile());
        runScriptResult.setRefProjectResource(taskItem.getRefProjectResource());
        return runScriptResult;
    }

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

    /**
     * 预生成用例的执行报告
     *
     * @param apiTestCase
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String initApiReport(String taskItemId, ApiTestCase apiTestCase, GetRunScriptRequest request) {
        // 初始化报告
        ApiReport apiReport = getApiReport(apiTestCase, request);
        if (StringUtils.isBlank(apiReport.getId())) {
            apiReport.setId(IDGenerator.nextStr());
        }
        apiReportService.insertApiReport(apiReport);
        return initApiReportDetail(taskItemId, apiTestCase, apiReport.getId());
    }

    /**
     * 预生成用例的执行报告
     *
     * @param apiTestCase
     * @return
     */
    public String initApiReportDetail(String taskItemId, ApiTestCase apiTestCase, String reportId) {
        // 初始化步骤
        ApiReportStep apiReportStep = getApiReportStep(apiTestCase, reportId, 1);
        // 初始化报告和用例的关联关系
        ApiTestCaseRecord apiTestCaseRecord = getApiTestCaseRecord(apiTestCase, reportId);
        // 初始化报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = apiCommonService.getApiReportRelateTask(taskItemId, reportId);

        apiReportService.insertApiReportDetail(apiReportStep, apiTestCaseRecord, apiReportRelateTask);
        return apiTestCaseRecord.getApiReportId();
    }

    public ApiReport getApiReport(ApiTestCase apiTestCase, GetRunScriptRequest request) {
        String reportId = request.getTaskItem().getReportId();
        ApiReport apiReport = getApiReport(request.getUserId());
        if (StringUtils.isNotBlank(reportId)) {
            apiReport.setId(reportId);
        }
        apiReport.setTriggerMode(request.getTriggerMode());
        apiReport.setName(apiTestCase.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiReport.setRunMode(request.getRunMode());
        apiReport.setPoolId(request.getPoolId());
        apiReport.setEnvironmentId(apiTestCase.getEnvironmentId());
        apiReport.setProjectId(apiTestCase.getProjectId());
        return apiReport;
    }

    public ApiReportStep getApiReportStep(ApiTestCase apiTestCase, String reportId, long sort) {
        return getApiReportStep(apiTestCase.getId(), apiTestCase.getName(), reportId, sort);
    }

    public ApiReportStep getApiReportStep(String stepId, String stepName, String reportId, long sort) {
        ApiReportStep apiReportStep = new ApiReportStep();
        apiReportStep.setReportId(reportId);
        apiReportStep.setStepId(stepId);
        apiReportStep.setSort(sort);
        apiReportStep.setName(stepName);
        apiReportStep.setStepType(ApiExecuteResourceType.API_CASE.name());
        return apiReportStep;
    }

    public ApiTestCaseRecord getApiTestCaseRecord(ApiTestCase apiTestCase, String reportId) {
        return getApiTestCaseRecord(apiTestCase.getId(), reportId);
    }

    public ApiTestCaseRecord getApiTestCaseRecord(String caseId, String reportId) {
        ApiTestCaseRecord apiTestCaseRecord = new ApiTestCaseRecord();
        apiTestCaseRecord.setApiTestCaseId(caseId);
        apiTestCaseRecord.setApiReportId(reportId);
        return apiTestCaseRecord;
    }

    public ApiReport getApiReport(String userId) {
        ApiReport apiReport = new ApiReport();
        apiReport.setId(IDGenerator.nextStr());
        apiReport.setDeleted(false);
        apiReport.setIntegrated(false);
        apiReport.setStartTime(System.currentTimeMillis());
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReport.setStatus(ExecStatus.RUNNING.name());
        apiReport.setUpdateUser(userId);
        apiReport.setCreateUser(userId);
        return apiReport;
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId, String runModule) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, resourceId);
        TaskInfo taskInfo = getTaskInfo(projectId, runModule);
        taskRequest.setTaskInfo(taskInfo);
        taskRequest.setTaskItem(taskItem);
        return taskRequest;
    }

    public TaskInfo getTaskInfo(String projectId, String runModule) {
        TaskInfo taskInfo = apiExecuteService.getTaskInfo(projectId);
        taskInfo.setResourceType(ApiExecuteResourceType.API_CASE.name());
        taskInfo.setRunMode(runModule);
        taskInfo.setNeedParseScript(false);
        return taskInfo;
    }
}
