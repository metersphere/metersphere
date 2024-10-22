package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.scenario.ApiScenarioBatchRunRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
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
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private BaseTaskHubService baseTaskHubService;

    public static final int TASK_BATCH_SIZE = 600;

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
    public void serialExecute(ApiScenarioBatchRunRequest request, String userId) {
        List<String> ids = apiScenarioService.doSelectIds(request, false);
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());

        // 初始化任务
        ExecTask execTask = initExecTask(ids, runModeConfig, project, userId);

        // 初始化集成报告
        if (runModeConfig.isIntegratedReport()) {
            initIntegratedReport(execTask.getId(), runModeConfig, userId, request.getProjectId());
        }

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionQueue(execTask.getId(), runModeConfig,
                ApiExecuteResourceType.API_SCENARIO.name(), null, userId);

        // 分批查询
        SubListUtils.dealForSubList(ids, TASK_BATCH_SIZE, subIds -> {
            List<ApiScenario> apiScenarios = getOrderScenarios(subIds, runModeConfig);

            // 初始化任务项
            List<ExecTaskItem> execTaskItems = initExecTaskItem(subIds, apiScenarios, userId, project, execTask);

            if (runModeConfig.isIntegratedReport()) {
                String reportId = runModeConfig.getCollectionReport().getReportId();
                // 初始化集合报告和场景的关联关系
                initIntegratedReportCaseRecord(reportId, subIds);
                // 集合报告初始化一级步骤
                initApiScenarioReportStep(apiScenarios, reportId);
            }

            // 初始化队列项
            apiBatchRunBaseService.initExecutionQueueDetails(queue.getQueueId(), execTaskItems);
        });

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

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());

        // 初始化任务
        ExecTask execTask = initExecTask(ids, runModeConfig, project, userId);

        if (runModeConfig.isIntegratedReport()) {
            // 初始化集成报告
            initIntegratedReport(execTask.getId(), runModeConfig, userId, request.getProjectId());
        }

        // 分批查询
        SubListUtils.dealForSubList(ids, TASK_BATCH_SIZE, subIds -> {
            List<ApiScenario> apiScenarios = getOrderScenarios(subIds, runModeConfig);
            Map<String, String> caseReportMap = null;

            // 初始化任务项
            Map<String, String> resourceExecTaskItemMap = initExecTaskItem(subIds, apiScenarios, userId, project, execTask)
                    .stream()
                    .collect(Collectors.toMap(ExecTaskItem::getResourceId, ExecTaskItem::getId));

            if (runModeConfig.isIntegratedReport()) {
                // 集合报告初始化一级步骤
                initApiScenarioReportStep(apiScenarios, runModeConfig.getCollectionReport().getReportId());
            } else {
                // 非集合报告，初始化独立报告，执行时初始化步骤
                caseReportMap = initScenarioReport(resourceExecTaskItemMap, runModeConfig, apiScenarios, userId);
            }

            List<TaskItem> taskItems = new ArrayList<>(ids.size());

            for (ApiScenario apiScenario : apiScenarios) {
                // 如果是集成报告则生成唯一的虚拟ID，非集成报告使用单用例的报告ID
                String reportId = runModeConfig.isIntegratedReport()
                        ? runModeConfig.getCollectionReport().getReportId() + IDGenerator.nextStr()
                        : caseReportMap.get(apiScenario.getId());

                TaskItem taskItem = apiExecuteService.getTaskItem(reportId, apiScenario.getId());
                taskItem.setId(resourceExecTaskItemMap.get(apiScenario.getId()));
                taskItem.setRequestCount(1L);
                taskItems.add(taskItem);
            }

            // 记录任务项，用于统计整体执行情况
            apiExecutionSetService.initSet(execTask.getId(), new ArrayList<>(resourceExecTaskItemMap.values()));

            TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(request.getProjectId(), runModeConfig);
            taskRequest.getTaskInfo().setTaskId(execTask.getId());
            taskRequest.getTaskInfo().setUserId(userId);
            taskRequest.setTaskItems(taskItems);
            apiExecuteService.batchExecute(taskRequest);
        });
    }

    /**
     * 获取有序的用例
     * @param ids
     * @return
     */
    private List<ApiScenario> getOrderScenarios(List<String> ids, ApiRunModeConfigDTO runModeConfig) {
        List<ApiScenario> apiScenarios = new ArrayList<>(TASK_BATCH_SIZE);
        // 分批查询
        List<ApiScenario> finalApiScenarios = apiScenarios;
        SubListUtils.dealForSubList(ids, 200, subIds -> finalApiScenarios.addAll(extApiScenarioMapper.getScenarioExecuteInfoByIds(subIds)));
        Map<String, ApiScenario> apiScenarioMap = apiScenarios.stream()
                .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));

        apiScenarios = new ArrayList<>(ids.size());

        for (String id : ids) {
            // 按照ID顺序排序
            ApiScenario apiScenario = apiScenarioMap.get(id);
            if (apiScenario == null) {
                if (runModeConfig.isIntegratedReport()) {
                    // 用例不存在，则在执行集合中删除
                    apiExecutionSetService.removeItem(runModeConfig.getCollectionReport().getReportId(), id);
                }
                LogUtils.info("当前执行任务的用例已删除 {}", id);
                break;
            }
            apiScenarios.add(apiScenario);
        }
        return apiScenarios;
    }

    private ExecTask initExecTask(List<String> ids, ApiRunModeConfigDTO runModeConfig, Project project, String userId) {
        ExecTask execTask = apiCommonService.newExecTask(project.getId(), userId);
        execTask.setCaseCount(Long.valueOf(ids.size()));
        if (runModeConfig.isIntegratedReport()) {
            execTask.setTaskName(runModeConfig.getCollectionReport().getReportName());
        } else {
            execTask.setTaskName(Translator.get("api_scenario_batch_task_name", ApiBatchRunBaseService.getLocale()));
        }
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(TaskTriggerMode.BATCH.name());
        execTask.setTaskType(ExecTaskType.API_SCENARIO_BATCH.name());
        execTask.setIntegrated(runModeConfig.isIntegratedReport());
        baseTaskHubService.insertExecTask(execTask);
        return execTask;
    }

    private List<ExecTaskItem> initExecTaskItem(List<String> ids, List<ApiScenario> apiScenarios, String userId, Project project, ExecTask execTask) {
        List<ExecTaskItem> execTaskItems = new ArrayList<>(ids.size());
        for (ApiScenario apiScenario : apiScenarios) {
            ExecTaskItem execTaskItem = apiCommonService.newExecTaskItem(execTask.getId(), project.getId(), userId);
            execTaskItem.setOrganizationId(project.getOrganizationId());
            execTaskItem.setResourceType(ApiExecuteResourceType.API_SCENARIO.name());
            execTaskItem.setResourceId(apiScenario.getId());
            execTaskItem.setResourceName(apiScenario.getName());
            execTaskItems.add(execTaskItem);
        }
        baseTaskHubService.insertExecTaskDetail(execTaskItems);
        return execTaskItems;
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
     * @return
     */
    private ApiScenarioReport initIntegratedReport(String taskId, ApiRunModeConfigDTO runModeConfig, String userId, String projectId) {
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, userId);
        apiScenarioReport.setName(runModeConfig.getCollectionReport().getReportName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setIntegrated(true);
        apiScenarioReport.setProjectId(projectId);

        // 创建报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
        apiReportRelateTask.setReportId(apiScenarioReport.getId());
        apiReportRelateTask.setTaskResourceId(taskId);

        apiScenarioReportService.insertApiScenarioReport(apiScenarioReport, apiReportRelateTask);
        // 设置集成报告执行参数
        runModeConfig.getCollectionReport().setReportId(apiScenarioReport.getId());
        return apiScenarioReport;
    }

    private void initIntegratedReportCaseRecord(String reportId, List<String> ids) {
        // 初始化集成报告与用例的关联关系
        List<ApiScenarioRecord> records = ids.stream().map(id -> {
            ApiScenarioRecord scenarioRecord = new ApiScenarioRecord();
            scenarioRecord.setApiScenarioReportId(reportId);
            scenarioRecord.setApiScenarioId(id);
            return scenarioRecord;
        }).toList();
        apiScenarioReportService.insertApiScenarioReport(List.of(), records);
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
            reportId = runModeConfig.getCollectionReport().getReportId() + IDGenerator.nextStr();
        } else {
            // 独立报告，执行到当前任务时初始化报告
            reportId = initScenarioReport(queueDetail.getTaskItemId(), runModeConfig, apiScenario, queue.getUserId()).getApiScenarioReportId();
        }

        TaskRequestDTO taskRequest = getTaskRequestDTO(apiScenario.getProjectId(), queue.getRunModeConfig());
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, queueDetail.getResourceId());
        taskItem.setId(queueDetail.getTaskItemId());
        taskRequest.setTaskItem(taskItem);
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());

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
        taskInfo.setBatch(true);
        return apiBatchRunBaseService.setBatchRunTaskInfoParam(runModeConfig, taskInfo);
    }

    public Map<String, String> initScenarioReport(Map<String, String> resourceExecTaskItemMap, ApiRunModeConfigDTO runModeConfig,
                                                  List<ApiScenario> apiScenarios, String userId) {
        List<ApiScenarioReport> apiScenarioReports = new ArrayList<>(apiScenarios.size());
        List<ApiScenarioRecord> apiScenarioRecords = new ArrayList<>(apiScenarios.size());
        List<ApiReportRelateTask> apiReportRelateTasks = new ArrayList<>();
        for (ApiScenario apiScenario : apiScenarios) {
            // 初始化报告
            ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, apiScenario, userId);
            apiScenarioReport.setId(IDGenerator.nextStr());
            apiScenarioReports.add(apiScenarioReport);
            // 创建报告和用例的关联关系
            ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(apiScenario, apiScenarioReport);
            apiScenarioRecords.add(apiScenarioRecord);
            // 创建报告和任务的关联关系
            ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
            apiReportRelateTask.setReportId(apiScenarioReport.getId());
            apiReportRelateTask.setTaskResourceId(resourceExecTaskItemMap.get(apiScenario.getId()));
            apiReportRelateTasks.add(apiReportRelateTask);
        }
        apiScenarioReportService.insertApiScenarioReport(apiScenarioReports, apiScenarioRecords, apiReportRelateTasks);
        return apiScenarioRecords.stream().collect(Collectors.toMap(ApiScenarioRecord::getApiScenarioId, ApiScenarioRecord::getApiScenarioReportId));
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param apiScenario
     * @return
     */
    public ApiScenarioRecord initScenarioReport(String taskItemId, ApiRunModeConfigDTO runModeConfig, ApiScenario apiScenario, String userId) {
        // 初始化报告
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, apiScenario, userId);
        apiScenarioReport.setId(IDGenerator.nextStr());
        // 创建报告和用例的关联关系
        ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(apiScenario, apiScenarioReport);

        // 创建报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
        apiReportRelateTask.setReportId(apiScenarioReport.getId());
        apiReportRelateTask.setTaskResourceId(taskItemId);

        apiScenarioReportService.insertApiScenarioReport(List.of(apiScenarioReport), List.of(apiScenarioRecord), List.of(apiReportRelateTask));
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
            report.setStatus(ResultStatus.ERROR.name());
            report.setExecStatus(ExecStatus.COMPLETED.name());
            apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        } catch (Exception e) {
            LogUtils.error("失败停止，补充报告步骤失败：", e);
        }
    }
}
