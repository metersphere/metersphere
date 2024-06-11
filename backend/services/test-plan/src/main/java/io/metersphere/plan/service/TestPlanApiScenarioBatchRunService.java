package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.dto.request.TestPlanApiScenarioBatchRunRequest;
import io.metersphere.plan.mapper.ExtTestPlanApiScenarioMapper;
import io.metersphere.plan.mapper.TestPlanApiScenarioMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.TaskTriggerMode;
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
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiScenarioBatchRunService {
    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioBatchRunService apiScenarioBatchRunService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private TestPlanMapper testPlanMapper;

    /**
     * 异步批量执行
     *
     * @param request
     * @param userId
     */
    public void asyncBatchRun(TestPlanApiScenarioBatchRunRequest request, String userId) {
        Thread.startVirtualThread(() -> batchRun(request, userId));
    }

    /**
     * 批量执行
     *
     * @param request
     * @param userId
     */
    private void batchRun(TestPlanApiScenarioBatchRunRequest request, String userId) {
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
    public void serialExecute(TestPlanApiScenarioBatchRunRequest request, String userId) throws Exception {
//        List<String> ids = testPlanApiScenarioService.doSelectIds(request, false); //todo
        List<String> ids = request.getSelectIds();
        // todo 查询测试规划
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name(), userId);
        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
        executeNextTask(queue, nextDetail);
    }

    /**
     * 并行批量执行
     *
     * @param request
     */
    public void parallelExecute(TestPlanApiScenarioBatchRunRequest request, String userId) {
//        List<String> ids = testPlanApiScenarioService.doSelectIds(request, false); //todo
        List<String> ids = request.getSelectIds();

        // todo 查询测试规划
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);

        Map<String, String> scenarioReportMap = initReport(ids, runModeConfig, userId);

        List<TaskItem> taskItems = ids.stream()
                .map(id -> apiExecuteService.getTaskItem(scenarioReportMap.get(id), id)).toList();

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());

        TaskBatchRequestDTO taskRequest = getTaskBatchRequestDTO(testPlan.getProjectId(), runModeConfig);
        taskRequest.setTaskItems(taskItems);

        apiExecuteService.batchExecute(taskRequest);
    }

    private ApiRunModeConfigDTO getRunModeConfig(TestPlanApiScenarioBatchRunRequest request) {
        ApiRunModeConfigDTO runModeConfig = BeanUtils.copyBean(new ApiRunModeConfigDTO(), request.getRunModeConfig());
        return runModeConfig;
    }

    private Map<String, String> initReport(List<String> ids, ApiRunModeConfigDTO runModeConfig, String userId) {
        List<TestPlanApiScenario> testPlanApiScenarios = new ArrayList<>(ids.size());

        List<ApiScenario> apiScenarios = new ArrayList<>(ids.size());
        // 分批查询
        List<TestPlanApiScenario> finalTestPlanApiScenarios = testPlanApiScenarios;
        SubListUtils.dealForSubList(ids, 100, subIds -> finalTestPlanApiScenarios.addAll(extTestPlanApiScenarioMapper.getScenarioExecuteInfoByIds(subIds)));

        List<String> caseIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getApiScenarioId).toList();
        List<ApiScenario> finalApiScenarios = apiScenarios;
        SubListUtils.dealForSubList(caseIds, 100, subIds -> finalApiScenarios.addAll(extApiScenarioMapper.getScenarioExecuteInfoByIds(subIds)));

        Map<String, ApiScenario> apiScenarioMap = apiScenarios.stream()
                .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));

        Map<String, TestPlanApiScenario> testPlanApiScenarioMap = testPlanApiScenarios.stream()
                .collect(Collectors.toMap(TestPlanApiScenario::getId, Function.identity()));

        testPlanApiScenarios = new ArrayList<>(ids.size());
        for (String id : ids) {
            // 按照ID顺序排序
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMap.get(id);
            if (testPlanApiScenario == null) {
                break;
            }
            testPlanApiScenarios.add(testPlanApiScenario);
        }
        // 初始化独立报告，执行时初始化步骤
        return initScenarioReport(runModeConfig, testPlanApiScenarios, apiScenarioMap, userId);
    }

    public Map<String, String> initScenarioReport(ApiRunModeConfigDTO runModeConfig, List<TestPlanApiScenario> testPlanApiScenarios,
                                                  Map<String, ApiScenario> apiScenarioMap, String userId) {
        List<ApiScenarioReport> apiScenarioReports = new ArrayList<>(testPlanApiScenarios.size());
        List<ApiScenarioRecord> apiScenarioRecords = new ArrayList<>(testPlanApiScenarios.size());
        for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
            ApiScenario apiScenario = apiScenarioMap.get(testPlanApiScenario.getApiScenarioId());
            // 初始化报告
            ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, testPlanApiScenario, apiScenario, userId);
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
     * 执行串行的下一个任务
     *
     * @param queue
     * @param queueDetail
     */
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        ApiRunModeConfigDTO runModeConfig = queue.getRunModeConfig();
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(queueDetail.getResourceId());
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());

        // 独立报告，执行到当前任务时初始化报告
        String reportId = initScenarioReport(runModeConfig, testPlanApiScenario, apiScenario, queue.getUserId()).getApiScenarioReportId();

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

    private TaskInfo getTaskInfo(String projectId, ApiRunModeConfigDTO runModeConfig) {
        TaskInfo taskInfo = apiScenarioBatchRunService.getTaskInfo(projectId, runModeConfig);
        taskInfo.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name());
        return taskInfo;
    }

    /**
     * 预生成用例的执行报告
     *
     * @param runModeConfig
     * @param apiScenario
     * @return
     */
    public ApiScenarioRecord initScenarioReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiScenario testPlanApiScenario,
                                                ApiScenario apiScenario, String userId) {
        // 初始化报告
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, testPlanApiScenario, apiScenario, userId);
        apiScenarioReport.setId(IDGenerator.nextStr());
        // 创建报告和用例的关联关系
        ApiScenarioRecord apiScenarioRecord = apiScenarioRunService.getApiScenarioRecord(apiScenario, apiScenarioReport);
        apiScenarioReportService.insertApiScenarioReport(List.of(apiScenarioReport), List.of(apiScenarioRecord));
        return apiScenarioRecord;
    }

    public String getEnvId(ApiRunModeConfigDTO runModeConfig, TestPlanApiScenario testPlanApiScenario) {
        return StringUtils.isBlank(runModeConfig.getEnvironmentId()) ? testPlanApiScenario.getEnvironmentId() : runModeConfig.getEnvironmentId();
    }

    private ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, TestPlanApiScenario testPlanApiScenario, ApiScenario apiScenario, String userId) {
        ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, userId);
        apiScenarioReport.setEnvironmentId(apiScenarioRunService.getEnvId(runModeConfig, apiScenario));
        apiScenarioReport.setName(apiScenario.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setProjectId(apiScenario.getProjectId());
        apiScenarioReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        apiScenarioReport.setTestPlanScenarioId(testPlanApiScenario.getId());
        apiScenarioReport.setEnvironmentId(getEnvId(runModeConfig, testPlanApiScenario));
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
}
