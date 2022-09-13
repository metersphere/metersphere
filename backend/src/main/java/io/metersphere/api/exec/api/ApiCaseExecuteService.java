package io.metersphere.api.exec.api;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ApiCaseRunRequest;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.RunModeConfigWithEnvironmentDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.utils.ApiDefinitionExecResultUtil;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.service.ApiCaseResultService;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.ApiScenarioReportStructureService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiCaseExecuteService {
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
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
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiCaseResultService apiCaseResultService;
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;

    /**
     * 测试计划case执行
     *
     * @param request
     * @return
     */
    public List<MsExecResponseDTO> run(BatchRunDefinitionRequest request) {
        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        if (CollectionUtils.isEmpty(request.getPlanIds())) {
            return responseDTOS;
        }
        if (request.getConfig() == null) {
            request.setConfig(new RunModeConfigDTO());
        }
        if (StringUtils.equals("GROUP", request.getConfig().getEnvironmentType()) && StringUtils.isNotEmpty(request.getConfig().getEnvironmentGroupId())) {
            request.getConfig().setEnvMap(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }
        LoggerUtil.debug("开始查询测试计划用例");

        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(request.getPlanIds());
        example.setOrderByClause("`order` DESC");
        List<TestPlanApiCase> planApiCases = testPlanApiCaseMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(planApiCases)) {
            return responseDTOS;
        }
        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ApiRunMode.API_PLAN.name());
        }
        LoggerUtil.debug("查询到测试计划用例 " + planApiCases.size());

        Map<String, ApiDefinitionExecResultWithBLOBs> executeQueue = request.isRerun() ? request.getExecuteQueue() : new LinkedHashMap<>();
        String status = request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString()) ? APITestStatus.Waiting.name() : APITestStatus.Running.name();

        // 查出用例
        List<String> apiCaseIds = planApiCases.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
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
            Map<String, String> planProjects = new HashMap<>();
            for (TestPlanApiCase testPlanApiCase : planApiCases) {
                //处理环境配置为空时的情况
                RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
                BeanUtils.copyBean(runModeConfigDTO, request.getConfig());
                if (MapUtils.isEmpty(runModeConfigDTO.getEnvMap())) {
                    ApiTestCase testCase = caseMap.get(testPlanApiCase.getApiCaseId());
                    if (testCase != null) {
                        runModeConfigDTO.setEnvMap(new HashMap<>() {{
                            this.put(testCase.getProjectId(), testPlanApiCase.getEnvironmentId());
                        }});
                    }
                }
                ApiDefinitionExecResultWithBLOBs report = ApiDefinitionExecResultUtil.addResult(request, runModeConfigDTO, testPlanApiCase, status, caseMap, resourcePoolId);
                if (planProjects.containsKey(testPlanApiCase.getTestPlanId())) {
                    report.setProjectId(planProjects.get(testPlanApiCase.getTestPlanId()));
                } else {
                    TestPlan plan = CommonBeanFactory.getBean(TestPlanMapper.class).selectByPrimaryKey(testPlanApiCase.getTestPlanId());
                    if (plan != null) {
                        planProjects.put(plan.getId(), plan.getProjectId());
                        report.setProjectId(plan.getProjectId());
                    }
                }
                executeQueue.put(testPlanApiCase.getId(), report);
                responseDTOS.add(new MsExecResponseDTO(testPlanApiCase.getId(), report.getId(), request.getTriggerMode()));
                LoggerUtil.debug("预生成测试用例结果报告：" + report.getName() + ", ID " + report.getId());
            }
            apiCaseResultService.batchSave(executeQueue);
        }

        LoggerUtil.debug("开始生成测试计划队列");
        String reportType = request.getConfig().getReportType();
        String poolId = request.getConfig().getResourcePoolId();
        String runMode = StringUtils.equals(request.getTriggerMode(), TriggerMode.MANUAL.name()) ? ApiRunMode.API_PLAN.name() : ApiRunMode.SCHEDULE_API_PLAN.name();
        DBTestQueue deQueue = apiExecutionQueueService.add(executeQueue, poolId, ApiRunMode.API_PLAN.name(), request.getPlanReportId(), reportType, runMode, request.getConfig());

        // 开始选择执行模式
        if (deQueue != null && deQueue.getDetail() != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("PLAN-CASE：" + request.getPlanReportId());
                    if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                        apiCaseSerialService.serial(deQueue);
                    } else {
                        apiCaseParallelExecuteService.parallel(executeQueue, request.getConfig(), deQueue, runMode);
                    }
                }
            });
            thread.start();
        }
        return responseDTOS;
    }

    public Map<String, List<String>> checkEnv(List<ApiTestCaseWithBLOBs> caseList) {
        Map<String, List<String>> projectEnvMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            StringBuilder builderHttp = new StringBuilder();
            StringBuilder builderTcp = new StringBuilder();
            for (int i = caseList.size() - 1; i >= 0; i--) {
                ApiTestCaseWithBLOBs apiCase = caseList.get(i);
                JSONObject apiCaseNew = new JSONObject(apiCase.getRequest());
                if (apiCaseNew.has("type") && "HTTPSamplerProxy".equals(apiCaseNew.getString("type"))) {
                    if (!apiCaseNew.has("useEnvironment") || StringUtils.isEmpty(apiCaseNew.getString("useEnvironment"))) {
                        builderHttp.append(apiCase.getName()).append("; ");
                    } else {
                        //记录运行环境ID
                        String envId = apiCaseNew.getString("useEnvironment");
                        if (projectEnvMap.containsKey(apiCase.getProjectId())) {
                            if (!projectEnvMap.get(apiCase.getProjectId()).contains(envId)) {
                                projectEnvMap.get(apiCase.getProjectId()).add(envId);
                            }
                        } else {
                            projectEnvMap.put(apiCase.getProjectId(), new ArrayList<>() {{
                                this.add(envId);
                            }});
                        }
                    }
                }
                if (apiCaseNew.has("type") && "JDBCSampler".equals(apiCaseNew.getString("type"))) {
                    DatabaseConfig dataSource = null;
                    if (apiCaseNew.has("useEnvironment") && apiCaseNew.has("dataSourceId")) {
                        String environmentId = apiCaseNew.getString("useEnvironment");
                        String dataSourceId = apiCaseNew.getString("dataSourceId");
                        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                        ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentId);
                        EnvironmentConfig envConfig = null;
                        if (environment != null && environment.getConfig() != null) {
                            envConfig = com.alibaba.fastjson.JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                            if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                                for (DatabaseConfig item : envConfig.getDatabaseConfigs()) {
                                    if (item.getId().equals(dataSourceId)) {
                                        dataSource = item;
                                        //记录运行环境ID
                                        if (projectEnvMap.containsKey(apiCase.getProjectId())) {
                                            if (!projectEnvMap.get(apiCase.getProjectId()).contains(environmentId)) {
                                                projectEnvMap.get(apiCase.getProjectId()).add(environmentId);
                                            }
                                        } else {
                                            projectEnvMap.put(apiCase.getProjectId(), new ArrayList<>() {{
                                                this.add(environmentId);
                                            }});
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (dataSource == null) {
                        builderTcp.append(apiCase.getName()).append("; ");
                    }
                }
            }
            if (StringUtils.isNotEmpty(builderHttp)) {
                MSException.throwException("用例：" + builderHttp + "运行环境为空！请检查");
            }
            if (StringUtils.isNotEmpty(builderTcp)) {
                MSException.throwException("用例：" + builderTcp + "数据源为空！请检查");
            }
        }
        return projectEnvMap;
    }

    public Map<String, String> getEnvMap(ApiTestCaseWithBLOBs apiCase) {
        Map<String, String> projectEnvMap = new HashMap<>();

        JSONObject apiCaseNew = new JSONObject(apiCase.getRequest());
        if (apiCaseNew.has("type") && "HTTPSamplerProxy".equals(apiCaseNew.getString("type"))) {
            if (apiCaseNew.has("useEnvironment") && StringUtils.isNotEmpty(apiCaseNew.getString("useEnvironment"))) {
                //记录运行环境ID
                String envId = apiCaseNew.getString("useEnvironment");
                projectEnvMap.put(apiCase.getProjectId(), envId);
            }
        }
        if (apiCaseNew.has("type") && "JDBCSampler".equals(apiCaseNew.getString("type"))) {
            if (apiCaseNew.has("useEnvironment") && apiCaseNew.has("dataSourceId")) {
                String environmentId = apiCaseNew.getString("useEnvironment");
                String dataSourceId = apiCaseNew.getString("dataSourceId");
                ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentId);
                EnvironmentConfig envConfig = null;
                if (environment != null && environment.getConfig() != null) {
                    envConfig = com.alibaba.fastjson.JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                        for (DatabaseConfig item : envConfig.getDatabaseConfigs()) {
                            if (item.getId().equals(dataSourceId)) {
                                //记录运行环境ID
                                projectEnvMap.put(apiCase.getProjectId(), environmentId);
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

        if (StringUtils.equals(EnvironmentType.GROUP.toString(), request.getConfig().getEnvironmentType()) && StringUtils.isNotEmpty(request.getConfig().getEnvironmentGroupId())) {
            request.getConfig().setEnvMap(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }

        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiTestCaseMapper.selectIdsByQuery((ApiTestCaseRequest) query));

        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        List<ApiTestCaseWithBLOBs> caseList = extApiTestCaseMapper.unTrashCaseListByIds(request.getIds());
        LoggerUtil.debug("查询到执行数据：" + caseList.size());
        Map<String, List<String>> testCaseEnvMap = new HashMap<>();
        // 环境检查
        if (MapUtils.isEmpty(request.getConfig().getEnvMap())) {
            testCaseEnvMap = this.checkEnv(caseList);
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
            ApiDefinitionExecResultWithBLOBs report = ApiDefinitionExecResultUtil.initBase(null, APITestStatus.Running.name(), serialReportId, config);
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
            result.setStatus(APITestStatus.Rerunning.name());
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

        String status = request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString()) ? APITestStatus.Waiting.name() : APITestStatus.Running.name();
        // 第一次执行非重跑的报告处理
        if (!request.isRerun()) {
            for (int i = 0; i < caseList.size(); i++) {
                ApiTestCaseWithBLOBs caseWithBLOBs = caseList.get(i);

                RunModeConfigDTO config = new RunModeConfigDTO();
                BeanUtils.copyBean(config, request.getConfig());

                if (StringUtils.equals(config.getEnvironmentType(), EnvironmentType.JSON.name()) && MapUtils.isEmpty(config.getEnvMap())) {
                    config.setEnvMap(this.getEnvMap(caseWithBLOBs));
                }
                ApiDefinitionExecResultWithBLOBs report = ApiDefinitionExecResultUtil.initBase(caseWithBLOBs.getId(), APITestStatus.Running.name(), null, config);
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
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("API-CASE-RUN");
                    if (request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                        apiCaseSerialService.serial(queue);
                    } else {
                        apiCaseParallelExecuteService.parallel(executeQueue, request.getConfig(), queue, ApiRunMode.DEFINITION.name());
                    }
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
