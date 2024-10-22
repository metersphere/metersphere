package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
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
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public static final int TASK_BATCH_SIZE = 600;

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

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());

        // 初始化任务
        ExecTask execTask = initExecTask(ids, runModeConfig, project, userId);

        if (runModeConfig.isIntegratedReport()) {
            // 初始化集成报告
            initIntegratedReport(execTask.getId(), runModeConfig, userId, request.getProjectId());
        }

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionQueue(execTask.getId(), runModeConfig, ApiExecuteResourceType.API_CASE.name(), null, userId);

        // 分批查询
        SubListUtils.dealForSubList(ids, TASK_BATCH_SIZE, subIds -> {
            List<ApiTestCase> apiTestCases = getOrderApiTestCases(subIds, runModeConfig);

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

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());

        executeNextTask(queue, nextDetail);
    }

    private List<ExecTaskItem> initExecTaskItem(List<ApiTestCase> apiTestCases, String userId, Project project, ExecTask execTask) {
        List<ExecTaskItem> execTaskItems = new ArrayList<>(apiTestCases.size());
        for (ApiTestCase apiTestCase : apiTestCases) {
            ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(execTask.getId(), project.getId(), userId);
            execTaskItem.setOrganizationId(project.getOrganizationId());
            execTaskItem.setResourceType(ApiExecuteResourceType.API_CASE.name());
            execTaskItem.setResourceId(apiTestCase.getId());
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

        // 分批查询
        SubListUtils.dealForSubList(ids, TASK_BATCH_SIZE, subIds -> {
            List<ApiTestCase> apiTestCases = getOrderApiTestCases(subIds, runModeConfig);
            // 初始化任务项
            Map<String, String> resourceExecTaskItemMap = initExecTaskItem(apiTestCases, userId, project, execTask)
                    .stream()
                    .collect(Collectors.toMap(ExecTaskItem::getResourceId, ExecTaskItem::getId));

            // 初始化报告，返回用例和报告的 map
            Map<String, String> caseReportMap = initParallelReport(resourceExecTaskItemMap, runModeConfig, apiTestCases, userId);

            List<TaskItem> taskItems = new ArrayList<>(subIds.size());

            for (ApiTestCase apiTestCase : apiTestCases) {
                // 如果是集成报告则生成唯一的虚拟ID，非集成报告使用单用例的报告ID
                String reportId = runModeConfig.isIntegratedReport()
                        ? runModeConfig.getCollectionReport().getReportId() + IDGenerator.nextStr()
                        : caseReportMap.get(apiTestCase.getId());

                TaskItem taskItem = apiExecuteService.getTaskItem(reportId, apiTestCase.getId());
                taskItem.setId(resourceExecTaskItemMap.get(apiTestCase.getId()));
                taskItem.setRequestCount(1L);
                taskItems.add(taskItem);
            }

            // 记录任务项，用于统计整体执行情况
            apiExecutionSetService.initSet(execTask.getId(), new ArrayList<>(resourceExecTaskItemMap.values()));

            TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(request.getProjectId(), runModeConfig);
            taskRequest.getTaskInfo().setTaskId(execTask.getId());
            taskRequest.setTaskItems(taskItems);
            apiExecuteService.batchExecute(taskRequest);
        });
    }

    /**
     * 获取有序的用例
     * @param ids
     * @return
     */
    private List<ApiTestCase> getOrderApiTestCases(List<String> ids, ApiRunModeConfigDTO runModeConfig) {
        List<ApiTestCase> apiTestCases = new ArrayList<>(TASK_BATCH_SIZE);
        // 分批查询
        List<ApiTestCase> finalApiTestCases = apiTestCases;
        SubListUtils.dealForSubList(ids, 200, subIds -> finalApiTestCases.addAll(extApiTestCaseMapper.getApiCaseExecuteInfoByIds(subIds)));
        Map<String, ApiTestCase> apiCaseMap = apiTestCases.stream()
                .collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));

        apiTestCases = new ArrayList<>(ids.size());

        for (String id : ids) {
            // 按照ID顺序排序
            ApiTestCase apiTestCase = apiCaseMap.get(id);
            if (apiTestCase == null) {
                if (runModeConfig.isIntegratedReport()) {
                    // 用例不存在，则在执行集合中删除
                    apiExecutionSetService.removeItem(runModeConfig.getCollectionReport().getReportId(), id);
                }
                LogUtils.info("当前执行任务的用例已删除 {}", id);
                break;
            }
            apiTestCases.add(apiTestCase);
        }
        return apiTestCases;
    }

    private Map<String, String> initParallelReport(Map<String, String> resourceExecTaskItemMap, ApiRunModeConfigDTO runModeConfig, List<ApiTestCase> apiTestCases, String userId) {
        // 先初始化所有报告
        if (runModeConfig.isIntegratedReport()) {
            // 获取集成报告ID
            String integratedReportId = runModeConfig.getCollectionReport().getReportId();
            initApiReportSteps(apiTestCases, integratedReportId);
            return null;
        } else {
            // 初始化非集成报告
            List<ApiTestCaseRecord> apiTestCaseRecords = initApiReport(resourceExecTaskItemMap, runModeConfig, apiTestCases, userId);
            return apiTestCaseRecords.stream()
                    .collect(Collectors.toMap(ApiTestCaseRecord::getApiTestCaseId, ApiTestCaseRecord::getApiReportId));
        }
    }

    /**
     * 初始化集成报告的报告步骤
     *
     * @param reportId
     */
    private void initApiReportSteps(List<ApiTestCase> apiTestCases, String reportId) {
        AtomicLong sort = new AtomicLong(1);
        List<ApiReportStep> apiReportSteps = apiTestCases.stream()
                .map(apiTestCase -> getApiReportStep(apiTestCase, reportId, sort.getAndIncrement()))
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

        String reportId;
        if (runModeConfig.isIntegratedReport()) {
            reportId = runModeConfig.getCollectionReport().getReportId() + IDGenerator.nextStr();
        } else {
            // 独立报告，执行到当前任务时初始化报告
            reportId = initApiReport(Map.of(apiTestCase.getId(), taskItemId), runModeConfig, List.of(apiTestCase), queue.getUserId()).getFirst().getApiReportId();
        }

        if (apiTestCase == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", resourceId);
            return;
        }

        TaskRequestDTO taskRequest = getTaskRequestDTO(reportId, apiTestCase, runModeConfig);
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskItem().setRequestCount(1L);
        taskRequest.getTaskItem().setId(taskItemId);

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
        taskInfo.setBatch(true);
        return apiBatchRunBaseService.setBatchRunTaskInfoParam(runModeConfig, taskInfo);
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param apiTestCases
     * @return
     */
    public List<ApiTestCaseRecord> initApiReport(Map<String, String> resourceExecTaskItemMap,
                                                 ApiRunModeConfigDTO runModeConfig, List<ApiTestCase> apiTestCases, String userId) {
        List<ApiReport> apiReports = new ArrayList<>();
        List<ApiTestCaseRecord> apiTestCaseRecords = new ArrayList<>();
        List<ApiReportStep> apiReportSteps = new ArrayList<>();
        List<ApiReportRelateTask> apiReportRelateTasks = new ArrayList<>();
        for (ApiTestCase apiTestCase : apiTestCases) {
            // 初始化报告
            ApiReport apiReport = getApiReport(runModeConfig, apiTestCase, userId);
            apiReports.add(apiReport);
            // 创建报告和用例的关联关系
            ApiTestCaseRecord apiTestCaseRecord = apiTestCaseService.getApiTestCaseRecord(apiTestCase, apiReport);
            apiTestCaseRecords.add(apiTestCaseRecord);
            apiReportSteps.add(getApiReportStep(apiTestCase, apiReport.getId(), 1));

            // 创建报告和任务的关联关系
            ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
            apiReportRelateTask.setReportId(apiReport.getId());
            apiReportRelateTask.setTaskResourceId(resourceExecTaskItemMap.get(apiTestCase.getId()));
            apiReportRelateTasks.add(apiReportRelateTask);
        }

        apiReportService.insertApiReport(apiReports, apiTestCaseRecords, apiReportRelateTasks);
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
            report.setStatus(ResultStatus.ERROR.name());
            report.setExecStatus(ExecStatus.COMPLETED.name());
            apiReportMapper.updateByPrimaryKeySelective(report);
        }
    }
}
