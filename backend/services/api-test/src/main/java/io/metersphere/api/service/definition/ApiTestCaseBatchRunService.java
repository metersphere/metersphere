package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiDefinitionExecuteInfo;
import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.definition.ApiTestCaseBatchRunRequest;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiTestCaseBlobMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ApiExecuteRunMode;
import io.metersphere.sdk.constants.TaskTriggerMode;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.api.task.CollectionReportDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private EnvironmentService environmentService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    /**
     * 异步批量执行
     * @param request
     * @param userId
     */
    public void asyncBatchRun(ApiTestCaseBatchRunRequest request, String userId) {
        Thread.startVirtualThread(() -> batchRun(request, userId));
    }

    /**
     * 批量执行
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
    public void serialExecute(ApiTestCaseBatchRunRequest request, String userId) throws Exception {
        List<String> ids = apiTestCaseService.doSelectIds(request, false);
        ApiRunModeConfigDTO runModeConfig = getRunModeConfig(request);
        // 初始化集成报告
        if (runModeConfig.isIntegratedReport()) {
            initIntegratedReport(runModeConfig, ids, userId, request.getProjectId());
        }
        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionqueue(ids, runModeConfig, ApiExecuteResourceType.API_CASE.name(), userId);
        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
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

        AtomicInteger errorCount = new AtomicInteger();
        AtomicLong sort = new AtomicLong(1);

        // 分批处理
        SubListUtils.dealForSubList(ids, 100, subIds -> {
            List<ApiTestCase> apiTestCases = extApiTestCaseMapper.getApiCaseExecuteInfoByIds(subIds);
            // 获取用例和定义信息的map，key 为用例ID，value 为接口定义信息
            Map<String, ApiDefinitionExecuteInfo> definitionExecuteInfoMap = apiCommonService.getApiDefinitionExecuteInfoMap(apiTestCaseService::getModuleInfoByIds, subIds);

            Map<String, String> caseReportMap = null;
            String integratedReportId = null;

            Map<String, ApiTestCase> apiCaseMap = apiTestCases.stream()
                    .collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));

            ApiTestCaseBlobExample example = new ApiTestCaseBlobExample();
            example.createCriteria().andIdIn(subIds);
            Map<String, ApiTestCaseBlob> apiTestCaseBlobMap = apiTestCaseBlobMapper.selectByExampleWithBLOBs(example).stream()
                    .collect(Collectors.toMap(ApiTestCaseBlob::getId, Function.identity()));

            if (runModeConfig.isIntegratedReport()) {
                // 获取集成报告ID
                integratedReportId = runModeConfig.getCollectionReport().getReportId();
                initApiReportSteps(subIds, apiCaseMap, integratedReportId, sort);
            } else {
                // 初始化非集成报告
                List<ApiTestCaseRecord> apiTestCaseRecords = initApiReport(runModeConfig, apiTestCases, userId);
                caseReportMap = apiTestCaseRecords.stream()
                        .collect(Collectors.toMap(ApiTestCaseRecord::getApiTestCaseId, ApiTestCaseRecord::getApiReportId));
            }

            // 这里ID顺序和队列的ID顺序保持一致
            for (String id : subIds) {
                String reportId = null;
                try {
                    ApiTestCase apiTestCase = apiCaseMap.get(id);
                    ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMap.get(id);

                    if (apiTestCase == null) {
                        if (runModeConfig.isIntegratedReport()) {
                            // 用例不存在，则在执行集合中删除
                            apiExecutionSetService.removeItem(integratedReportId, id);
                        }
                        LogUtils.info("当前执行任务的用例已删除 {}", id);
                        break;
                    }

                    // 如果是集成报告则生成唯一的虚拟ID，非集成报告使用单用例的报告ID
                    reportId = runModeConfig.isIntegratedReport() ? UUID.randomUUID().toString() : caseReportMap.get(id);
                    TaskRequestDTO taskRequest = getTaskRequestDTO(reportId, apiTestCase, runModeConfig);
                    execute(taskRequest, apiTestCase, apiTestCaseBlob, definitionExecuteInfoMap.get(apiTestCase.getId()));
                } catch (Exception e) {
                    LogUtils.error("执行用例失败 {}-{}", reportId, id);
                    LogUtils.error(e);
                    if (errorCount.getAndIncrement() > 10) {
                        LogUtils.error("批量执行用例失败，错误次数超过10次，停止执行");
                        return;
                    }
                }
            }
        });
    }

    /**
     * 初始化集成报告的报告步骤
     *
     * @param ids
     * @param apiCaseMap
     * @param reportId
     */
    private void initApiReportSteps(List<String> ids, Map<String, ApiTestCase> apiCaseMap, String reportId, AtomicLong sort) {
        List<ApiReportStep> apiReportSteps = ids.stream()
                .map(id -> getApiReportStep(apiCaseMap.get(id), reportId, sort.getAndIncrement()))
                .collect(Collectors.toList());
        apiReportService.insertApiReportStep(apiReportSteps);
    }

    /**
     * 初始化集成报告的报告步骤
     */
    private void initApiReportSteps(ApiTestCase apiTestCase, String reportId, long sort) {
        ApiReportStep apiReportStep = getApiReportStep(apiTestCase, reportId, sort);
        apiReportService.insertApiReportStep(List.of(apiReportStep));
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
        apiReport.setName(runModeConfig.getCollectionReport().getReportName());
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
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(resourceId);

        if (apiTestCase == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", resourceId);
            return;
        }
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiTestCase.getApiDefinitionId());

        String reportId;
        if (runModeConfig.isIntegratedReport()) {
            String integratedReportId = runModeConfig.getCollectionReport().getReportId();
            initApiReportSteps(apiTestCase, integratedReportId, queueDetail.getSort());
            reportId = UUID.randomUUID().toString();
        } else {
            reportId = initApiReport(runModeConfig, List.of(apiTestCase), queue.getUserId()).get(0).getApiReportId();
        }
        TaskRequestDTO taskRequest = getTaskRequestDTO(reportId, apiTestCase, runModeConfig);
        taskRequest.setQueueId(queue.getQueueId());
        execute(taskRequest, apiTestCase, apiTestCaseBlob, BeanUtils.copyBean(new ApiDefinitionExecuteInfo(), apiDefinition));
    }

    /**
     * 执行批量的单个任务
     *
     * @param apiTestCase
     * @param apiTestCaseBlob
     */
    public void execute(TaskRequestDTO taskRequest, ApiTestCase apiTestCase, ApiTestCaseBlob apiTestCaseBlob, ApiDefinitionExecuteInfo definitionExecuteInfo) {
        ApiParamConfig apiParamConfig = apiExecuteService.getApiParamConfig(taskRequest.getReportId());
        ApiResourceRunRequest runRequest = new ApiResourceRunRequest();
        runRequest.setTestElement(ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class));

        // 设置环境信息
        apiParamConfig.setEnvConfig(environmentService.get(getEnvId(taskRequest.getRunModeConfig(), apiTestCase)));
        // 设置 method 等信息
        apiCommonService.setApiDefinitionExecuteInfo(runRequest.getTestElement(), definitionExecuteInfo);

        apiExecuteService.apiExecute(runRequest, taskRequest, apiParamConfig);
    }

    private TaskRequestDTO getTaskRequestDTO(String reportId, ApiTestCase apiTestCase, ApiRunModeConfigDTO runModeConfig) {
        TaskRequestDTO taskRequest = apiTestCaseService.getTaskRequest(reportId, apiTestCase.getId(), apiTestCase.getProjectId(), ApiExecuteRunMode.RUN.name());
        taskRequest.setSaveResult(true);
        taskRequest.setRealTime(false);
        taskRequest.setRunModeConfig(runModeConfig);
        return taskRequest;
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
        for (ApiTestCase apiTestCase : apiTestCases) {
            // 初始化报告
            ApiReport apiReport = getApiReport(runModeConfig, apiTestCase, userId);
            apiReports.add(apiReport);
            // 创建报告和用例的关联关系
            ApiTestCaseRecord apiTestCaseRecord = apiTestCaseService.getApiTestCaseRecord(apiTestCase, apiReport);
            apiTestCaseRecords.add(apiTestCaseRecord);
        }
        apiReportService.insertApiReport(apiReports, apiTestCaseRecords);
        return apiTestCaseRecords;
    }


    private ApiReport getApiReport(ApiRunModeConfigDTO runModeConfig, ApiTestCase apiTestCase, String userId) {
        ApiReport apiReport = getApiReport(runModeConfig, userId);
        apiReport.setEnvironmentId(getEnvId(runModeConfig, apiTestCase));
        apiReport.setName(apiTestCase.getName());
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

    /**
     * 获取执行的环境ID
     * 优先使用运行配置的环境
     * 没有则使用用例自身的环境
     *
     * @param runModeConfig
     * @param apiTestCase
     * @return
     */
    public String getEnvId(ApiRunModeConfigDTO runModeConfig, ApiTestCase apiTestCase) {
        return StringUtils.isBlank(runModeConfig.getEnvironmentId()) ? apiTestCase.getEnvironmentId() : runModeConfig.getEnvironmentId();
    }
}
