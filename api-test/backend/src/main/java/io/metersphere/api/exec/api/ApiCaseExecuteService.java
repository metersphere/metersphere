package io.metersphere.api.exec.api;


import io.metersphere.api.dto.ApiCaseRunRequest;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.RunModeConfigWithEnvironmentDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.plan.TestPlanApiCaseInfoDTO;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.service.ApiExecutionQueueService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.service.definition.ApiCaseResultService;
import io.metersphere.service.scenario.ApiScenarioReportStructureService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiCaseExecuteService {
    @Resource
    private ApiCaseSerialService apiCaseSerialService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiCaseParallelExecuteService apiCaseParallelExecuteService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private BaseEnvGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiCaseResultService apiCaseResultService;
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private BaseEnvironmentService baseEnvironmentService;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private RedisTemplateService redisTemplateService;

    /**
     * 测试计划case执行
     *
     */
    public List<MsExecResponseDTO> run(BatchRunDefinitionRequest request) {
        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        if (request.getConfig() == null) {
            request.setConfig(new RunModeConfigDTO());
        }
        if (StringUtils.equals(EnvironmentType.GROUP.name(), request.getConfig().getEnvironmentType())
                && StringUtils.isNotEmpty(request.getConfig().getEnvironmentGroupId())) {
            request.getConfig().setEnvMap(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }
        List<TestPlanApiCaseInfoDTO> planApiCases;
        if (CollectionUtils.isNotEmpty(request.getPlanCaseIds())) {
            planApiCases = extTestPlanApiCaseMapper.selectByPlanCaseIds(request.getPlanCaseIds());
        } else {
            planApiCases = extTestPlanApiCaseMapper.selectLegalDataByTestPlanId(request.getTestPlanId());
        }
        if (CollectionUtils.isEmpty(planApiCases)) {
            return responseDTOS;
        }
        LoggerUtil.info("查询到测试计划用例：" + planApiCases.size(), request.getPlanReportId());

        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ApiRunMode.API_PLAN.name());
        }
        Map<String, ApiDefinitionExecResultWithBLOBs> executeQueue = request.isRerun() ? request.getExecuteQueue() : new LinkedHashMap<>();

        String status = StringUtils.equals(request.getConfig().getMode(), RunModeConstants.SERIAL.toString())
                ? ApiReportStatus.PENDING.name() : ApiReportStatus.RUNNING.name();

        // 查出用例
        List<String> apiCaseIds = planApiCases.stream().map(TestPlanApiCaseInfoDTO::getApiCaseId).collect(Collectors.toList());
        ApiTestCaseExample caseExample = new ApiTestCaseExample();
        caseExample.createCriteria().andIdIn(apiCaseIds);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(caseExample);
        Map<String, ApiTestCase> caseMap = apiTestCases.stream().collect(Collectors.toMap(ApiTestCase::getId, a -> a, (k1, k2) -> k1));
        // 资源池
        String resourcePoolId = null;
        if (request.getConfig() != null && GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()).isPool()) {
            resourcePoolId = request.getConfig().getResourcePoolId();
        }
        if (!request.isRerun()) {
            for (TestPlanApiCaseInfoDTO testPlanApiCase : planApiCases) {
                //处理环境配置为空时的情况
                RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
                BeanUtils.copyBean(runModeConfigDTO, request.getConfig());
                ApiTestCase testCase = caseMap.get(testPlanApiCase.getApiCaseId());
                if (testCase == null) {
                    continue;
                }
                if (MapUtils.isEmpty(runModeConfigDTO.getEnvMap())) {
                    runModeConfigDTO.setEnvMap(new HashMap<>() {{
                        this.put(testCase.getProjectId(), testPlanApiCase.getEnvironmentId());
                    }});
                }
                ApiDefinitionExecResultWithBLOBs report = ApiDefinitionExecResultUtil.addResult(request, runModeConfigDTO, testPlanApiCase, status, testCase, resourcePoolId);
                executeQueue.put(testPlanApiCase.getId(), report);
                responseDTOS.add(new MsExecResponseDTO(testPlanApiCase.getId(), report.getId(), request.getTriggerMode()));
                // 执行中资源锁住，防止重复更新造成LOCK WAIT
                redisTemplateService.lock(testPlanApiCase.getId(), report.getId());

                LoggerUtil.info("预生成测试用例结果报告：" + report.getName(), report.getId());
            }
            apiCaseResultService.batchSave(executeQueue);
        }

        LoggerUtil.info("开始生成测试计划队列", request.getPlanReportId());
        String reportType = request.getConfig().getReportType();
        String poolId = request.getConfig().getResourcePoolId();
        String runMode = StringUtils.equals(request.getTriggerMode(), TriggerMode.MANUAL.name()) ? ApiRunMode.API_PLAN.name() : ApiRunMode.SCHEDULE_API_PLAN.name();
        DBTestQueue deQueue = apiExecutionQueueService.add(executeQueue, poolId, ApiRunMode.API_PLAN.name(), request.getPlanReportId(), reportType, runMode, request.getConfig());

        // 开始选择执行模式
        if (deQueue != null && deQueue.getDetail() != null) {
            Thread thread = new Thread(() -> {
                Thread.currentThread().setName("PLAN-CASE：" + request.getPlanReportId());
                if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                    apiCaseSerialService.serial(deQueue);
                } else {
                    apiCaseParallelExecuteService.parallel(executeQueue, request.getConfig(), deQueue, runMode);
                }
            });
            thread.start();
        }
        return responseDTOS;
    }

    public Map<String, List<String>> getRequestEnv(List<ApiTestCaseWithBLOBs> caseList) {
        Map<String, List<String>> projectEnvMap = new HashMap<>();
        if (CollectionUtils.isEmpty(caseList)) {
            return projectEnvMap;
        }
        StringBuilder message = new StringBuilder();
        for (ApiTestCaseWithBLOBs apiCase : caseList) {
            JSONObject requestObj = new JSONObject(apiCase.getRequest());
            if (StringUtils.equalsAny(requestObj.optString(PropertyConstant.TYPE), ElementConstants.HTTP_SAMPLER, ElementConstants.JDBC_SAMPLER)) {
                Map<String, String> envMap = this.getEnvMap(requestObj, apiCase.getProjectId());
                if (MapUtils.isEmpty(envMap)) {
                    message.append(apiCase.getName()).append("；");
                } else {
                    //记录运行环境ID
                    this.setEnvId(projectEnvMap, apiCase.getProjectId(), envMap.get(apiCase.getProjectId()));
                }
            }
        }
        if (StringUtils.isNotEmpty(message)) {
            // message过长时截取部分展示
            String[] splitErr = message.toString().split("；");
            if (splitErr.length > 20) {
                MSException.throwException("用例：" + String.join("；", Arrays.copyOf(splitErr, 20)) + "....；运行环境为空！");
            }
            MSException.throwException("用例：" + message + "运行环境为空！");
        }
        return projectEnvMap;
    }

    private void setEnvId(Map<String, List<String>> projectEnvMap, String projectId, String envId) {
        //记录运行环境ID
        if (projectEnvMap.containsKey(projectId)) {
            if (!projectEnvMap.get(projectId).contains(envId)) {
                projectEnvMap.get(projectId).add(envId);
            }
        } else {
            projectEnvMap.put(projectId, new ArrayList<>() {{
                this.add(envId);
            }});
        }
    }

    public Map<String, String> getEnvMap(JSONObject request, String projectId) {
        Map<String, String> projectEnvMap = new HashMap<>();
        if (!request.has(PropertyConstant.TYPE)) {
            return projectEnvMap;
        }
        if (StringUtils.equals(ElementConstants.HTTP_SAMPLER, request.optString(PropertyConstant.TYPE))) {
            if (StringUtils.isNotEmpty(request.optString(PropertyConstant.ENVIRONMENT))) {
                //记录运行环境ID
                projectEnvMap.put(projectId, request.optString(PropertyConstant.ENVIRONMENT));
            }
        }
        if (StringUtils.equals(ElementConstants.JDBC_SAMPLER, request.optString(PropertyConstant.TYPE))) {
            if (request.has(PropertyConstant.ENVIRONMENT) && request.has(PropertyConstant.DATASOURCE_ID)) {
                ApiTestEnvironmentWithBLOBs environment = baseEnvironmentService.get(request.optString(PropertyConstant.ENVIRONMENT));
                if (environment != null && environment.getConfig() != null) {
                    EnvironmentConfig envConfig = JSON.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                        for (DatabaseConfig item : envConfig.getDatabaseConfigs()) {
                            if (StringUtils.equals(item.getId(), request.optString(PropertyConstant.DATASOURCE_ID))) {
                                //记录运行环境ID
                                projectEnvMap.put(projectId, request.optString(PropertyConstant.ENVIRONMENT));
                            }
                        }
                    }
                }
            }
        }
        return projectEnvMap;
    }

    /**
     * 接口定义case执行
     *
     * @param request
     * @return
     */
    public List<MsExecResponseDTO> run(ApiCaseRunRequest request) {
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("进入执行方法，接收到参数：" + JSON.toJSONString(request));
        }
        if (request.getConfig() == null) {
            request.setConfig(new RunModeConfigDTO());
        }
        jMeterService.verifyPool(request.getProjectId(), request.getConfig());

        if (StringUtils.equals(EnvironmentType.GROUP.name(), request.getConfig().getEnvironmentType())
                && StringUtils.isNotEmpty(request.getConfig().getEnvironmentGroupId())) {
            request.getConfig().setEnvMap(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }

        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiTestCaseMapper.selectIdsByQuery((ApiTestCaseRequest) query));

        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        if (CollectionUtils.isEmpty(request.getIds())) {
            return responseDTOS;
        }
        List<ApiTestCaseWithBLOBs> caseList = extApiTestCaseMapper.unTrashCaseListByIds(request.getIds());
        // 检查执行内容合规性
        PerformInspectionUtil.caseInspection(caseList);

        LoggerUtil.debug("查询到执行数据：" + caseList.size());
        Map<String, List<String>> testCaseEnvMap = new HashMap<>();
        // 环境检查
        if (MapUtils.isEmpty(request.getConfig().getEnvMap())) {
            testCaseEnvMap = this.getRequestEnv(caseList);
        }
        // 集合报告设置
        String serialReportId = request.isRerun() ? request.getReportId() : null;
        if (!request.isRerun() && StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString())
                && StringUtils.isNotEmpty(request.getConfig().getReportName())) {
            serialReportId = UUID.randomUUID().toString();

            RunModeConfigDTO config = request.getConfig();
            if (MapUtils.isEmpty(config.getEnvMap())) {
                RunModeConfigWithEnvironmentDTO runModeConfig = new RunModeConfigWithEnvironmentDTO();
                BeanUtils.copyBean(runModeConfig, request.getConfig());
                this.setExecutionEnvironment(runModeConfig, testCaseEnvMap);
                config = runModeConfig;
            }
            ApiDefinitionExecResultWithBLOBs report = ApiDefinitionExecResultUtil.initBase(null, ApiReportStatus.RUNNING.name(), serialReportId, config);
            report.setName(request.getConfig().getReportName());
            report.setProjectId(request.getProjectId());
            report.setReportType(ReportTypeConstants.API_INTEGRATED.name());
            report.setVersionId(caseList.get(0).getVersionId());

            Map<String, ApiDefinitionExecResultWithBLOBs> executeQueue = new LinkedHashMap<>();
            executeQueue.put(serialReportId, report);

            apiScenarioReportStructureService.save(serialReportId, new ArrayList<>());
            apiCaseResultService.batchSave(executeQueue);
            responseDTOS.add(new MsExecResponseDTO(JSON.toJSONString(request.getIds()), report.getId(), request.getTriggerMode()));
        }
        // 失败重跑报告状态更新
        if (request.isRerun()) {
            ApiDefinitionExecResultWithBLOBs result = new ApiDefinitionExecResultWithBLOBs();
            result.setId(serialReportId);
            result.setStatus(ApiReportStatus.RERUNNING.name());
            apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);
        }

        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            if (request.getCondition() == null || !request.getCondition().isSelectAll()) {
                // 按照id指定顺序排序
                FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(request.getIds());
                fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
                BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
                Collections.sort(caseList, beanComparator);
            }
        }

        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ApiRunMode.DEFINITION.name());
        }
        // 重试数据直接获取执行的报告
        Map<String, ApiDefinitionExecResultWithBLOBs> executeQueue = request.isRerun() ? request.getExecuteQueue() : new LinkedHashMap<>();

        String status = request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString()) ?
                ApiReportStatus.PENDING.name() : ApiReportStatus.RUNNING.name();
        // 第一次执行非重跑的报告处理
        if (!request.isRerun()) {
            for (int i = 0; i < caseList.size(); i++) {
                ApiTestCaseWithBLOBs caseWithBLOBs = caseList.get(i);

                RunModeConfigDTO config = new RunModeConfigDTO();
                BeanUtils.copyBean(config, request.getConfig());

                if (StringUtils.equals(config.getEnvironmentType(), EnvironmentType.JSON.name()) && MapUtils.isEmpty(config.getEnvMap())) {
                    JSONObject jsonObject = new JSONObject(caseWithBLOBs.getRequest());
                    config.setEnvMap(this.getEnvMap(jsonObject, caseWithBLOBs.getProjectId()));
                }
                ApiDefinitionExecResultWithBLOBs report = ApiDefinitionExecResultUtil.initBase(caseWithBLOBs.getId(), ApiReportStatus.RUNNING.name(), null, config);
                report.setStatus(status);
                report.setName(caseWithBLOBs.getName());
                report.setProjectId(caseWithBLOBs.getProjectId());
                report.setVersionId(caseWithBLOBs.getVersionId());
                report.setCreateTime(System.currentTimeMillis() + i);
                if (StringUtils.isNotEmpty(serialReportId)) {
                    report.setIntegratedReportId(serialReportId);
                }
                executeQueue.put(caseWithBLOBs.getId(), report);
                if (!StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    responseDTOS.add(new MsExecResponseDTO(caseWithBLOBs.getId(), report.getId(), request.getTriggerMode()));
                }
            }
            apiCaseResultService.batchSave(executeQueue);
        }

        String reportType = request.getConfig().getReportType();
        String poolId = request.getConfig().getResourcePoolId();
        DBTestQueue queue = apiExecutionQueueService.add(executeQueue, poolId, ApiRunMode.DEFINITION.name(), serialReportId, reportType, ApiRunMode.DEFINITION.name(), request.getConfig());
        // 开始选择执行模式
        if (queue != null && queue.getDetail() != null) {
            Thread thread = new Thread(() -> {
                Thread.currentThread().setName("API-CASE-RUN");
                if (request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                    apiCaseSerialService.serial(queue);
                } else {
                    apiCaseParallelExecuteService.parallel(executeQueue, request.getConfig(), queue, ApiRunMode.DEFINITION.name());
                }
            });
            thread.start();
        }
        return responseDTOS;
    }

    public void setExecutionEnvironment(RunModeConfigWithEnvironmentDTO config, Map<String, List<String>> projectEnvMap) {
        if (MapUtils.isNotEmpty(projectEnvMap) && config != null) {
            config.setExecutionEnvironmentMap(projectEnvMap);
        }
    }

    public void setRunModeConfigEnvironment(RunModeConfigWithEnvironmentDTO config, Map<String, String> projectEnvMap) {
        if (MapUtils.isNotEmpty(projectEnvMap) && config != null) {
            Map<String, List<String>> executionEnvMap = new HashMap<>();
            for (Map.Entry<String, String> entry : projectEnvMap.entrySet()) {
                String projectId = entry.getKey();
                String envId = entry.getValue();
                if (StringUtils.isNoneEmpty(projectId, envId)) {
                    executionEnvMap.put(projectId, new ArrayList<>() {{
                        this.add(envId);
                    }});
                }
            }
            if (MapUtils.isNotEmpty(executionEnvMap)) {
                config.setExecutionEnvironmentMap(executionEnvMap);
            }
        }
    }
}
