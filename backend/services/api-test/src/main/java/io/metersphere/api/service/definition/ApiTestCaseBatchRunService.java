package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiResourceBatchRunInfo;
import io.metersphere.api.dto.definition.ApiTestCaseBatchRunRequest;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import io.metersphere.system.service.BaseTaskHubService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    private ApiTestCaseRunService apiTestCaseRunService;
    @Resource
    private ApiCommonService apiCommonService;
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
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;

    public static final int TASK_BATCH_SIZE = 600;

    /**
     * 批量执行
     *
     * @param request
     * @param userId
     */
    public void batchRun(ApiTestCaseBatchRunRequest request, String userId) {
        if (StringUtils.equals(request.getRunModeConfig().getRunMode(), ApiBatchRunMode.PARALLEL.name())) {
            parallelExecute(request, userId);
        } else {
            serialExecute(request, userId);
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

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());

        // 初始化任务
        ExecTask execTask = initExecTask(ids, runModeConfig, project, userId);

        if (runModeConfig.isIntegratedReport()) {
            // 初始化集成报告
            initIntegratedReport(execTask.getId(), runModeConfig, userId, request.getProjectId());
        }

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionQueue(execTask.getId(), execTask.getId(), runModeConfig, ApiExecuteResourceType.API_CASE.name(), null, null, userId);

        // 分批查询
        SubListUtils.dealForSubList(ids, TASK_BATCH_SIZE, subIds -> {
            List<ApiResourceBatchRunInfo> apiTestCases = getOrderApiTestCases(subIds);

            // 初始化任务项
            List<ExecTaskItem> execTaskItems = initExecTaskItem(apiTestCases, userId, project, execTask);

            // 初始化队列项
            apiBatchRunBaseService.initExecutionQueueDetails(queue.getQueueId(), execTaskItems);

            if (runModeConfig.isIntegratedReport()) {
                String reportId = runModeConfig.getCollectionReport().getReportId();
                // 初始化集成报告和用例的关联关系
                initIntegratedReportCaseRecord(reportId, runModeConfig, subIds);

                // 初始化集成报告步骤
                initApiReportSteps(apiTestCases, reportId);
            }
        });

        Thread.startVirtualThread(() -> {
            // 执行第一个任务
            ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
            executeNextTask(queue, nextDetail);
        });
    }

    private List<ExecTaskItem> initExecTaskItem(List<ApiResourceBatchRunInfo> apiTestCases, String userId, Project project, ExecTask execTask) {
        List<ExecTaskItem> execTaskItems = new ArrayList<>(apiTestCases.size());
        for (ApiResourceBatchRunInfo apiTestCase : apiTestCases) {
            ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(execTask.getId(), project.getId(), userId);
            execTaskItem.setOrganizationId(project.getOrganizationId());
            execTaskItem.setResourceType(ApiExecuteResourceType.API_CASE.name());
            execTaskItem.setResourceId(apiTestCase.getId());
            execTaskItem.setCaseId(apiTestCase.getId());
            execTaskItem.setResourceName(apiTestCase.getName());
            execTaskItems.add(execTaskItem);
        }
        baseTaskHubService.insertExecTaskDetail(execTaskItems);
        return execTaskItems;
    }

    private ExecTask initExecTask(List<String> ids, ApiRunModeConfigDTO runModeConfig, Project project, String userId) {
        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(Long.valueOf(ids.size()));
        if (runModeConfig.isIntegratedReport()) {
            execTask.setTaskName(runModeConfig.getCollectionReport().getReportName());
        } else {
            execTask.setTaskName(Translator.get("api_batch_task_name"));
        }
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.BATCH.name());
        execTask.setTaskType(ExecTaskType.API_CASE_BATCH.name());
        execTask.setIntegrated(runModeConfig.getIntegratedReport());
        execTask.setPoolId(runModeConfig.getPoolId());
        execTask.setParallel(StringUtils.equals(runModeConfig.getRunMode(), ApiBatchRunMode.PARALLEL.name()));
        execTask.setEnvGrouped(runModeConfig.getGrouped());
        execTask.setEnvironmentId(runModeConfig.getEnvironmentId());
        baseTaskHubService.insertExecTask(execTask);
        return execTask;
    }

    /**
     * 并行批量执行
     *
     * @param request
     */
    public void parallelExecute(ApiTestCaseBatchRunRequest request, String userId) {
        List<String> ids = apiTestCaseService.doSelectIds(request, false);

        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());

        // 初始化任务
        ExecTask execTask = initExecTask(ids, runModeConfig, project, userId);

        if (runModeConfig.isIntegratedReport()) {
            // 初始化集成报告
            initIntegratedReport(execTask.getId(), runModeConfig, ids, userId, request.getProjectId());
        }

        // 记录用例和任务的映射
        Map<String, String> resourceExecTaskItemMap = new TreeMap<>();
        // 分批处理，初始化任务项
        SubListUtils.dealForSubList(ids, TASK_BATCH_SIZE, subIds -> {
            List<ApiResourceBatchRunInfo> apiTestCases = getOrderApiTestCases(subIds);
            // 初始化任务项
            List<ExecTaskItem> execTaskItems = initExecTaskItem(apiTestCases, userId, project, execTask);
            // 记录任务
            execTaskItems.forEach(item -> resourceExecTaskItemMap.put(item.getResourceId(), item.getId()));
            // 初始化集合报告步骤
            initInitApiReportSteps(runModeConfig, apiTestCases);
        });

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(request.getProjectId(), runModeConfig);
        taskRequest.getTaskInfo().setTaskId(execTask.getId());
        taskRequest.getTaskInfo().setSetId(execTask.getId());
        taskRequest.getTaskInfo().setUserId(userId);

        Thread.startVirtualThread(() -> {
            // 记录任务项，用于统计整体执行情况
            apiExecutionSetService.initSet(execTask.getId(), new ArrayList<>(resourceExecTaskItemMap.values()));
            apiBatchRunBaseService.parallelBatchExecute(taskRequest, runModeConfig, resourceExecTaskItemMap);
        });
    }

    /**
     * 获取有序的用例
     *
     * @param ids
     * @return
     */
    private List<ApiResourceBatchRunInfo> getOrderApiTestCases(List<String> ids) {
        List<ApiResourceBatchRunInfo> apiTestCases = new ArrayList<>(TASK_BATCH_SIZE);
        // 分批查询
        List<ApiResourceBatchRunInfo> finalApiTestCases = apiTestCases;
        SubListUtils.dealForSubList(ids, ApiBatchRunBaseService.SELECT_BATCH_SIZE, subIds -> finalApiTestCases.addAll(extApiTestCaseMapper.getApiCaseExecuteInfoByIds(subIds)));
        Map<String, ApiResourceBatchRunInfo> apiCaseMap = apiTestCases.stream()
                .collect(Collectors.toMap(ApiResourceBatchRunInfo::getId, Function.identity()));

        apiTestCases = new ArrayList<>(ids.size());

        for (String id : ids) {
            // 按照ID顺序排序
            ApiResourceBatchRunInfo apiTestCase = apiCaseMap.get(id);
            if (apiTestCase == null) {
                LogUtils.info("当前执行任务的用例已删除 {}", id);
                break;
            }
            apiTestCases.add(apiTestCase);
        }
        return apiTestCases;
    }

    private void initInitApiReportSteps(ApiRunModeConfigDTO runModeConfig, List<ApiResourceBatchRunInfo> apiTestCases) {
        // 先初始化所有报告
        if (runModeConfig.isIntegratedReport()) {
            // 获取集成报告ID
            String integratedReportId = runModeConfig.getCollectionReport().getReportId();
            initApiReportSteps(apiTestCases, integratedReportId);
        }
    }

    /**
     * 初始化集成报告的报告步骤
     *
     * @param reportId
     */
    private void initApiReportSteps(List<ApiResourceBatchRunInfo> apiTestCases, String reportId) {
        AtomicLong sort = new AtomicLong(1);
        List<ApiReportStep> apiReportSteps = apiTestCases.stream()
                .map(apiTestCase -> apiTestCaseRunService.getApiReportStep(apiTestCase.getId(), apiTestCase.getName(), reportId, sort.getAndIncrement()))
                .collect(Collectors.toList());
        apiReportService.insertApiReportStep(apiReportSteps);
    }

    private ApiRunModeConfigDTO getRunModeConfig(ApiTestCaseBatchRunRequest request) {
        ApiRunModeConfigDTO runModeConfig = BeanUtils.copyBean(new ApiRunModeConfigDTO(), request.getRunModeConfig());
        if (StringUtils.isNotBlank(request.getRunModeConfig().getIntegratedReportName()) && runModeConfig.isIntegratedReport()) {
            runModeConfig.setCollectionReport(new CollectionReportDTO());
            runModeConfig.getCollectionReport().setReportName(request.getRunModeConfig().getIntegratedReportName());
        }
        return runModeConfig;
    }

    public ApiRunModeConfigDTO getRunModeConfig(ExecTask execTask) {
        ApiRunModeConfigDTO runModeConfig = BeanUtils.copyBean(new ApiRunModeConfigDTO(), execTask);
        runModeConfig.setRunMode(BooleanUtils.isTrue(execTask.getParallel()) ? ApiBatchRunMode.PARALLEL.name() : ApiBatchRunMode.SERIAL.name());
        runModeConfig.setPoolId(execTask.getPoolId());
        runModeConfig.setEnvironmentId(execTask.getEnvironmentId());
        runModeConfig.setGrouped(execTask.getEnvGrouped());
        runModeConfig.setIntegratedReport(execTask.getIntegrated());
        if (BooleanUtils.isTrue(execTask.getIntegrated())) {
            runModeConfig.setCollectionReport(new CollectionReportDTO());
            runModeConfig.getCollectionReport().setReportId(apiBatchRunBaseService.getIntegratedReportId(execTask));
            runModeConfig.getCollectionReport().setReportName(execTask.getTaskName());
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
    private ApiReport initIntegratedReport(String taskId, ApiRunModeConfigDTO runModeConfig, List<String> ids, String userId, String projectId) {
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

        // 创建报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
        apiReportRelateTask.setReportId(apiReport.getId());
        apiReportRelateTask.setTaskResourceId(taskId);

        apiReportService.insertApiReport(List.of(apiReport), records, List.of(apiReportRelateTask));
        // 设置集成报告执行参数
        runModeConfig.getCollectionReport().setReportId(apiReport.getId());
        return apiReport;
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @return
     */
    private ApiReport initIntegratedReport(String taskId, ApiRunModeConfigDTO runModeConfig, String userId, String projectId) {
        ApiReport apiReport = getApiReport(runModeConfig, userId);
        apiReport.setName(runModeConfig.getCollectionReport().getReportName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiReport.setIntegrated(true);
        apiReport.setProjectId(projectId);

        // 创建报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
        apiReportRelateTask.setReportId(apiReport.getId());
        apiReportRelateTask.setTaskResourceId(taskId);

        apiReportService.insertApiReport(apiReport, apiReportRelateTask);
        // 设置集成报告执行参数
        runModeConfig.getCollectionReport().setReportId(apiReport.getId());
        return apiReport;
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param ids
     * @return
     */
    private void initIntegratedReportCaseRecord(String reportId, ApiRunModeConfigDTO runModeConfig, List<String> ids) {
        // 初始化集成报告与用例的关联关系
        List<ApiTestCaseRecord> records = ids.stream().map(id -> {
            ApiTestCaseRecord record = new ApiTestCaseRecord();
            record.setApiReportId(reportId);
            record.setApiTestCaseId(id);
            return record;
        }).toList();
        apiReportService.insertApiReport(List.of(), records);
        // 设置集成报告执行参数
        runModeConfig.getCollectionReport().setReportId(reportId);
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
        String taskItemId = queueDetail.getTaskItemId();

        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(resourceId);

        String integratedReportId = null;
        if (runModeConfig.isIntegratedReport()) {
            integratedReportId = runModeConfig.getCollectionReport().getReportId();
        }

        if (apiTestCase == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", resourceId);
            return;
        }

        TaskRequestDTO taskRequest = getTaskRequestDTO(integratedReportId, apiTestCase, runModeConfig);
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskInfo().setRerun(queue.getRerun());
        taskRequest.getTaskItem().setRequestCount(1L);
        taskRequest.getTaskItem().setId(taskItemId);

        try {
            apiExecuteService.execute(taskRequest);
        } catch (Exception e) {
            // 执行失败，删除队列
            apiExecutionQueueService.deleteQueue(queue.getQueueId());
        }
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
        taskInfo.setTriggerMode(TaskTriggerMode.BATCH.name());
        taskRequest.setTaskInfo(taskInfo);
        return taskRequest;
    }

    public TaskInfo getTaskInfo(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskInfo taskInfo = apiTestCaseRunService.getTaskInfo(projectId, ApiExecuteRunMode.RUN.name());
        taskInfo.setBatch(true);
        return apiBatchRunBaseService.setBatchRunTaskInfoParam(runModeConfig, taskInfo);
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param apiTestCase
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String initApiReport(String taskItemId, String reportId, ApiRunModeConfigDTO runModeConfig,
                                           ApiTestCase apiTestCase, String userId) {
        // 初始化报告
        ApiReport apiReport = getApiReport(runModeConfig, apiTestCase, userId);
        apiReport.setId(reportId);
        apiReportService.insertApiReport(apiReport);
        return apiTestCaseRunService.initApiReportDetail(taskItemId, apiTestCase, apiReport.getId());
    }

    public ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, ApiTestCase apiTestCase, String userId) {
        ApiReport apiReport = getApiReport(runModeConfig, userId);
        apiReport.setEnvironmentId(apiTestCaseRunService.getEnvId(runModeConfig, apiTestCase.getEnvironmentId()));
        apiReport.setName(apiTestCase.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiReport.setProjectId(apiTestCase.getProjectId());
        return apiReport;
    }

    public ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, String userId) {
        ApiReport apiReport = apiTestCaseRunService.getApiReport(userId);
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
            report.setStatus(ResultStatus.ERROR.name());
            report.setExecStatus(ExecStatus.COMPLETED.name());
            apiReportMapper.updateByPrimaryKeySelective(report);
        }
    }

    public void rerun(ExecTask execTask, String userId) {
        if (BooleanUtils.isTrue(execTask.getParallel())) {
            parallelRerunExecute(execTask, userId);
        } else {
            serialRerunExecute(execTask, userId);
        }
    }

    private void serialRerunExecute(ExecTask execTask, String userId) {
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(execTask);

        List<ExecTaskItem> execTaskItems = extExecTaskItemMapper.selectRerunIdAndResourceIdByTaskId(execTask.getId());

        // 初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.getExecutionQueue(runModeConfig, ApiExecuteResourceType.API_CASE.name(), execTask.getId(), userId);
        queue.setQueueId(execTask.getId());
        queue.setRerun(true);
        apiExecutionQueueService.insertQueue(queue);

        // 初始化队列项
        apiBatchRunBaseService.initExecutionQueueDetails(queue.getQueueId(), execTaskItems);

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行重跑
     *
     */
    public void parallelRerunExecute(ExecTask execTask, String userId) {
        String projectId = execTask.getProjectId();
        List<ExecTaskItem> execTaskItems = extExecTaskItemMapper.selectRerunIdAndResourceIdByTaskId(execTask.getId());
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(execTask);

        // 记录用例和任务的映射
        Map<String, String> resourceExecTaskItemMap = new TreeMap<>();
        execTaskItems.forEach(item -> resourceExecTaskItemMap.put(item.getResourceId(), item.getId()));

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(projectId, runModeConfig);
        taskRequest.getTaskInfo().setTaskId(execTask.getId());
        taskRequest.getTaskInfo().setSetId(execTask.getId());
        taskRequest.getTaskInfo().setUserId(userId);
        taskRequest.getTaskInfo().setRerun(true);

        // 记录任务项，用于统计整体执行情况
        apiExecutionSetService.initSet(execTask.getId(), new ArrayList<>(resourceExecTaskItemMap.values()));
        apiBatchRunBaseService.parallelBatchExecute(taskRequest, runModeConfig, resourceExecTaskItemMap);
    }
}
