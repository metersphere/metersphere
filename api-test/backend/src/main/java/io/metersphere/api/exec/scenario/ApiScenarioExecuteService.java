package io.metersphere.api.exec.scenario;

import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.RunModeConfigWithEnvironmentDTO;
import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.plan.TestPlanApiScenarioInfoDTO;
import io.metersphere.api.exec.api.ApiCaseExecuteService;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.NewDriverManager;
import io.metersphere.api.jmeter.utils.ApiFakeErrorUtil;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioExample;
import io.metersphere.base.domain.ApiScenarioReportWithBLOBs;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ApiTestConstants;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.commons.vo.RunPlanScenarioVO;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.i18n.Translator;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.ApiExecutionQueueService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.definition.TcpApiParamService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.service.scenario.ApiScenarioReportStructureService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioExecuteService {
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    protected ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    protected ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    protected ApiScenarioReportService apiScenarioReportService;
    @Resource
    private BaseEnvGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;
    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private ApiScenarioParallelService apiScenarioParallelService;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private ApiCaseExecuteService apiCaseExecuteService;
    @Resource
    protected JMeterService jMeterService;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private RedisTemplateService redisTemplateService;

    public List<MsExecResponseDTO> run(RunScenarioRequest request) {
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("Scenario run-执行脚本装载-接收到场景执行参数：【 " + JSON.toJSONString(request) + " 】");
        }
        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ReportTriggerMode.MANUAL.name());
        }
        if (request.getConfig() == null) {
            request.setConfig(new RunModeConfigDTO());
        }

        if (StringUtils.equals(EnvironmentType.GROUP.name(), request.getConfig().getEnvironmentType())
                && StringUtils.isNotEmpty(request.getConfig().getEnvironmentGroupId())) {
            request.getConfig().setEnvMap(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }

        // 生成集成报告id
        String serialReportId = request.isRerun() && GenerateHashTreeUtil.isSetReport(request.getConfig()) ? request.getSerialReportId() : null;
        LoggerUtil.info("Scenario run-执行脚本装载-根据条件查询所有场景 ");
        List<ApiScenarioWithBLOBs> apiScenarios = this.get(request);
        // 只有一个场景且没有测试步骤，则提示
        if (apiScenarios != null && apiScenarios.size() == 1 && (apiScenarios.get(0).getStepTotal() == null || apiScenarios.get(0).getStepTotal() == 0)) {
            MSException.throwException((apiScenarios.get(0).getName() + "，" + Translator.get("automation_exec_info")));
        }
        // 检查执行内容合规性
        PerformInspectionUtil.scenarioInspection(apiScenarios);
        // 环境检查
        LoggerUtil.info("Scenario run-执行脚本装载-开始针对所有执行场景进行环境检查");
        apiScenarioEnvService.checkEnv(request, apiScenarios);
        // 集合报告设置
        if (!request.isRerun() && GenerateHashTreeUtil.isSetReport(request.getConfig())) {
            if (isSerial(request)) {
                request.setExecuteType(ExecuteType.Completed.name());
            } else {
                request.setExecuteType(ExecuteType.Marge.name());
            }
            serialReportId = UUID.randomUUID().toString();
        }

        Map<String, RunModeDataDTO> executeQueue = new LinkedHashMap<>();
        LoggerUtil.info("Scenario run-执行脚本装载-初始化执行队列");
        if (StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name(),
                ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            //测试计划执行
            assemblyPlanScenario(request, executeQueue);
        } else {
            // 按照场景执行
            assemblyScenario(apiScenarios, request, executeQueue);
        }
        LoggerUtil.info("Scenario run-执行脚本装载-初始化执行队列完成：" + executeQueue.size());

        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        if (MapUtils.isEmpty(executeQueue)) {
            return responseDTOS;
        }
        // 集合报告处理
        if (GenerateHashTreeUtil.isSetReport(request.getConfig())) {
            this.setReport(request, serialReportId, apiScenarios, responseDTOS);
        }

        String reportType = request.getConfig().getReportType();
        String planReportId = StringUtils.isNotEmpty(request.getTestPlanReportId()) ? request.getTestPlanReportId() : serialReportId;
        // 生成执行队列
        DBTestQueue executionQueue = apiExecutionQueueService.add(
                executeQueue, request.getConfig().getResourcePoolId(),
                ApiRunMode.SCENARIO.name(), planReportId, reportType,
                request.getRunMode(), request.getConfig());

        // 预生成报告
        if (!request.isRerun() && !GenerateHashTreeUtil.isSetReport(request.getConfig())) {
            apiScenarioReportService.batchSave(executeQueue, serialReportId, request.getRunMode(), responseDTOS);
        }
        // 开始执行
        execute(request, serialReportId, executeQueue, executionQueue);
        return responseDTOS;
    }

    protected void execute(RunScenarioRequest request, String serialReportId, Map<String, RunModeDataDTO> executeQueue, DBTestQueue executionQueue) {
        Thread thread = new Thread(() -> {
            Thread.currentThread().setName("SCENARIO-THREAD");
            if (isSerial(request)) {
                apiScenarioSerialService.serial(executionQueue);
            } else {
                apiScenarioParallelService.parallel(executeQueue, request, serialReportId, executionQueue);
            }
        });
        thread.start();
    }

    protected boolean isSerial(RunScenarioRequest request) {
        return request.getConfig() != null && StringUtils.equals(request.getConfig().getMode(), RunModeConstants.SERIAL.toString());
    }


    protected String generateScenarioIds(List<String> scenarioIds) {
        return JSON.toJSONString(CollectionUtils.isNotEmpty(scenarioIds) && scenarioIds.size() > 50 ? scenarioIds.subList(0, 50) : scenarioIds);
    }

    public List<ApiScenarioWithBLOBs> get(RunScenarioRequest request) {
        RunPlanScenarioVO vo = new RunPlanScenarioVO();
        if (StringUtils.equalsAny(request.getRunMode(),
                ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            // 测试计划执行数据查询
            List<TestPlanApiScenarioInfoDTO> testPlanApiScenarioList;
            if (CollectionUtils.isNotEmpty(request.getPlanScenarioIds())) {
                testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectByPlanScenarioIds(request.getPlanScenarioIds(),
                        "\"" + StringUtils.join(request.getPlanScenarioIds(), ",") + "\"");

            } else {
                testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectLegalDataByTestPlanId(request.getTestPlanId());
            }
            if (CollectionUtils.isNotEmpty(testPlanApiScenarioList)) {
                List<String> ids = testPlanApiScenarioList.stream().map(TestPlanApiScenarioInfoDTO::getApiScenarioId).collect(Collectors.toList());
                request.setIds(ids);
                //这段代码之前的写法是开了testPlanApiScenarioList的流。但是这样造成的后果是获得了乱序的HashMap。而我们需要的是LinkedHashMap
                testPlanApiScenarioList.forEach(dto -> {
                    vo.getTestPlanScenarioMap().put(dto.getId(), dto);
                });

            }
        } else {
            ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        }
        List<String> ids = request.getIds();
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenarioWithBLOBs> apiScenarios = apiScenarioMapper.selectByExampleWithBLOBs(example);
        if (isSerial(request)) {
            if (request.getCondition() == null || !request.getCondition().isSelectAll()) {
                // 按照id指定顺序排序
                sortById(ids, apiScenarios);
            }
        }
        vo.setScenarioMap(apiScenarios.stream().collect(Collectors.toMap(ApiScenarioWithBLOBs::getId, Function.identity(), (t1, t2) -> t1)));
        request.setProcessVO(vo);
        return apiScenarios;
    }

    private void setReport(RunScenarioRequest request, String serialReportId, List<ApiScenarioWithBLOBs> apiScenarios, List<MsExecResponseDTO> responseDTOS) {
        // 失败重跑更新报告状态
        if (request.isRerun()) {
            ApiScenarioReportWithBLOBs report = new ApiScenarioReportWithBLOBs();
            report.setId(serialReportId);
            report.setStatus(ApiReportStatus.RERUNNING.name());
            apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        } else {
            LoggerUtil.info("Scenario run-执行脚本装载-初始化集成报告：" + serialReportId);
            request.getConfig().setReportId(UUID.randomUUID().toString());
            List<String> scenarioIds = new ArrayList<>();
            String reportScenarioIds = generateScenarioIds(scenarioIds);
            if (request.getConfig() == null) {
                request.setConfig(new RunModeConfigWithEnvironmentDTO());
            }
            if (MapUtils.isEmpty(request.getConfig().getEnvMap())) {
                RunModeConfigWithEnvironmentDTO runModeConfig = new RunModeConfigWithEnvironmentDTO();
                BeanUtils.copyBean(runModeConfig, request.getConfig());
                Map<String, List<String>> projectEnvMap = apiScenarioEnvService.selectApiScenarioEnv(apiScenarios);
                apiCaseExecuteService.setExecutionEnvironment(runModeConfig, projectEnvMap);
                request.setConfig(runModeConfig);
            }
            // 生成集合报告
            String names = apiScenarios.stream().map(ApiScenario::getName).collect(Collectors.joining(","));
            ApiScenarioReportResult report = apiScenarioReportService.getApiScenarioReportResult(request, serialReportId, names, reportScenarioIds);
            report.setVersionId(apiScenarios.get(0).getVersionId());
            apiScenarioReportMapper.insert(report);

            responseDTOS.add(new MsExecResponseDTO(JSON.toJSONString(scenarioIds), serialReportId, request.getRunMode()));
            apiScenarioReportStructureService.save(apiScenarios, serialReportId, request.getConfig().getReportType());
        }
    }

    protected void sortById(List<String> ids, List apiScenarios) {
        FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(ids);
        fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
        BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
        Collections.sort(apiScenarios, beanComparator);
    }

    /**
     * 测试计划接口场景的预执行（生成场景报告）
     */
    private void assemblyPlanScenario(RunScenarioRequest request, Map<String, RunModeDataDTO> executeQueue) {
        String reportId = request.getId();
        Map<String, ApiScenarioWithBLOBs> scenarioMap = request.getProcessVO().getScenarioMap();
        //检查环境组
        Map<String, String> configEnvMap = new HashMap<>();
        if (request.getConfig() != null) {
            apiScenarioEnvService.setScenarioEnv(request, configEnvMap);
        }
        for (String testPlanScenarioId : request.getProcessVO().getTestPlanScenarioMap().keySet()) {
            TestPlanApiScenarioInfoDTO planApiScenario = request.getProcessVO().getTestPlanScenarioMap().get(testPlanScenarioId);
            ApiScenarioWithBLOBs scenario = scenarioMap.get(planApiScenario.getApiScenarioId());
            Set<String> scenarioUsedProjectIdSet = null;
            try {
                scenarioUsedProjectIdSet = apiScenarioEnvService.getApiScenarioEnv(scenario.getScenarioDefinition()).getProjectIds();
            } catch (Exception ignore) {
            }
            if (scenario.getStepTotal() == null || scenario.getStepTotal() == 0) {
                continue;
            }
            if (StringUtils.isEmpty(request.getProjectId())) {
                request.setProjectId(scenario.getProjectId());
            }
            // 获取场景用例单独的执行环境
            Map<String, String> planEnvMap = apiScenarioEnvService.getPlanScenarioEnv(planApiScenario, configEnvMap);
            if (StringUtils.isEmpty(request.getProjectId())) {
                request.setProjectId(extTestPlanScenarioCaseMapper.getProjectIdById(testPlanScenarioId));
            }

            ApiScenarioReportResult report = apiScenarioReportService.initResult(reportId, testPlanScenarioId, scenario.getName(), request);
            if (request.isRerun()) {
                if (request.getReportMap().containsKey(planApiScenario.getApiScenarioId())) {
                    report = request.getReportMap().get(planApiScenario.getApiScenarioId());
                } else if (request.getReportMap().containsKey(testPlanScenarioId)) {
                    report = request.getReportMap().get(testPlanScenarioId);
                }
                if (report == null) {
                    return;
                }
            }

            report.setVersionId(scenario.getVersionId());
            RunModeDataDTO runModeDataDTO = getRunModeDataDTO(testPlanScenarioId, report);
            runModeDataDTO.setPlanEnvMap(planEnvMap);
            runModeDataDTO.setScenario(scenario);

            executeQueue.put(report.getId(), runModeDataDTO);

            if (ObjectUtils.isNotEmpty(request.getConfig())) {
                RunModeConfigWithEnvironmentDTO runModeConfig = new RunModeConfigWithEnvironmentDTO();
                BeanUtils.copyBean(runModeConfig, request.getConfig());
                if (MapUtils.isEmpty(runModeConfig.getEnvMap())) {
                    apiCaseExecuteService.setRunModeConfigEnvironment(runModeConfig, planEnvMap);
                }
                //对报告的envMap做过滤，通过场景用到的项目来进行匹配，过滤掉使用不到的项目环境
                if (CollectionUtils.isNotEmpty(scenarioUsedProjectIdSet)) {
                    List<String> scenarioUsedProjectIdList = new ArrayList<>(scenarioUsedProjectIdSet);
                    Map<String, String> diffEnvMap = new HashMap<>();
                    planEnvMap.forEach((k, v) -> {
                        if (scenarioUsedProjectIdList.contains(k)) {
                            diffEnvMap.put(k, v);
                        }
                    });
                    runModeConfig.setEnvMap(diffEnvMap);
                }
                report.setEnvConfig(JSON.toJSONString(runModeConfig));
            }
            // 生成文档结构
            if (!StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString())) {
                apiScenarioReportStructureService.save(scenario, report.getId(), request.getConfig() != null ? request.getConfig().getReportType() : null);
            }
            // 执行中资源锁住，防止重复更新造成LOCK WAIT
            redisTemplateService.lock(planApiScenario.getId(), report.getId());
            // 重置报告ID
            reportId = UUID.randomUUID().toString();
        }
    }

    /**
     * 接口场景的预执行（生成场景报告）
     */
    private void assemblyScenario(List<ApiScenarioWithBLOBs> apiScenarios, RunScenarioRequest request, Map<String, RunModeDataDTO> executeQueue) {
        String reportId = request.getId();
        for (int i = 0; i < apiScenarios.size(); i++) {
            ApiScenarioWithBLOBs item = apiScenarios.get(i);
            if (item.getStepTotal() == null || item.getStepTotal() == 0) {
                continue;
            }

            RunModeConfigWithEnvironmentDTO runModeConfig = new RunModeConfigWithEnvironmentDTO();
            BeanUtils.copyBean(runModeConfig, request.getConfig());
            if (StringUtils.equals(runModeConfig.getEnvironmentType(), EnvironmentType.JSON.name()) && MapUtils.isEmpty(runModeConfig.getEnvMap())) {
                Map<String, List<String>> projectEnvMap = apiScenarioEnvService.selectApiScenarioEnv(new ArrayList<>() {{
                    this.add(item);
                }});
                runModeConfig.setExecutionEnvironmentMap(projectEnvMap);
                request.setConfig(runModeConfig);
            }
            ApiScenarioReportResult report = apiScenarioReportService.initResult(reportId, item.getId(), item.getName(), request);
            report.setVersionId(item.getVersionId());

            RunModeDataDTO runModeDataDTO = getRunModeDataDTO(item.getId(), report);
            runModeDataDTO.setScenario(item);
            if (request.getConfig().getEnvMap() != null) {
                runModeDataDTO.setPlanEnvMap(request.getConfig().getEnvMap());
            }

            executeQueue.put(report.getId(), runModeDataDTO);
            // 生成报告结构
            if (!GenerateHashTreeUtil.isSetReport(request.getConfig())) {
                apiScenarioReportStructureService.save(item, report.getId(), request.getConfig().getReportType());
            }
            // 重置报告ID
            reportId = UUID.randomUUID().toString();
        }
    }

    protected RunModeDataDTO getRunModeDataDTO(String testId, ApiScenarioReportResult report) {
        RunModeDataDTO runModeDataDTO = new RunModeDataDTO();
        runModeDataDTO.setTestId(testId);
        runModeDataDTO.setPlanEnvMap(new HashMap<>());
        runModeDataDTO.setReport(report);
        runModeDataDTO.setReportId(report.getId());
        return runModeDataDTO;
    }

    public void testElement(RunDefinitionRequest request) {
        if (request.getTestElement() != null) {
            tcpApiParamService.checkTestElement(request.getTestElement());
        }
    }

    public String debug(RunDefinitionRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        if (StringUtils.equals(request.getEnvironmentType(), EnvironmentType.GROUP.toString())) {
            request.setEnvironmentMap(environmentGroupProjectService.getEnvMap(request.getEnvironmentGroupId()));
        }
        ParameterConfig config = new ParameterConfig();
        config.setScenarioId(request.getScenarioId());
        if (MapUtils.isNotEmpty(request.getEnvironmentMap())) {
            apiScenarioEnvService.setEnvConfig(request.getEnvironmentMap(), config);
        }
        if (request.isSaved()) {
            //记录环境
            if (request.getConfig() == null) {
                RunModeConfigDTO configDTO = new RunModeConfigDTO();
                configDTO.setEnvMap(request.getEnvironmentMap());
                request.setConfig(configDTO);
            }
            request.getConfig().setEnvMap(request.getEnvironmentMap());
            // 生成调试结果
            ApiScenarioReportResult report = apiScenarioReportService.initDebugResult(request);
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(request.getScenarioId());
            if (scenario != null) {
                report.setVersionId(scenario.getVersionId());
                String scenarioDefinition = JSON.toJSONString(request.getTestElement().getHashTree().get(0).getHashTree().get(0));
                scenario.setScenarioDefinition(scenarioDefinition);
                List<String> projectIdLists = ElementUtil.getProjectIds(scenarioDefinition);
                Map<String, String> envMap = ElementUtil.getProjectEnvMap(projectIdLists, request.getEnvironmentMap());
                request.getConfig().setEnvMap(envMap);
                report.setEnvConfig(JSON.toJSONString(request.getConfig()));
                apiScenarioReportStructureService.save(scenario, report.getId(), request.getConfig().getReportType());
            } else {
                if (request.getTestElement() != null && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree())) {
                    ApiScenarioWithBLOBs apiScenario = new ApiScenarioWithBLOBs();
                    apiScenario.setId(request.getScenarioId());
                    MsTestElement testElement = request.getTestElement().getHashTree().get(0).getHashTree().get(0);
                    if (testElement != null) {
                        apiScenario.setName(testElement.getName());
                        apiScenario.setScenarioDefinition(JSON.toJSONString(testElement));
                        List<String> projectIdLists =ElementUtil. getProjectIds(apiScenario.getScenarioDefinition());
                        Map<String, String> envMap = ElementUtil.getProjectEnvMap(projectIdLists, request.getEnvironmentMap());
                        request.getConfig().setEnvMap(envMap);
                        report.setEnvConfig(JSON.toJSONString(request.getConfig()));
                        apiScenarioReportStructureService.save(apiScenario, report.getId(), request.getConfig().getReportType());
                    }
                }
            }
            apiScenarioReportMapper.insert(report);
        }
        // 调用执行方法
        LoggerUtil.info("调用调试方法，开始执行", request.getId());
        uploadBodyFiles(request.getBodyFileRequestIds(), bodyFiles);
        FileUtils.createBodyFiles(request.getScenarioFileIds(), scenarioFiles);
        this.testElement(request);

        HashTree hashTree = request.getTestElement().generateHashTree(config);
        String runMode = StringUtils.isEmpty(request.getRunMode()) ? ApiRunMode.SCENARIO.name() : request.getRunMode();
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(request.getId(), request.getId(), runMode, hashTree);
        runRequest.setDebug(true);

        String jmx = request.getTestElement().getJmx(hashTree);
        LoggerUtil.info(jmx);
        PerformInspectionUtil.inspection(jmx, request.getScenarioId(), 0);
        if (request.getConfig() != null && StringUtils.isNotEmpty(request.getConfig().getResourcePoolId())) {
            runRequest.setPool(GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()));
            runRequest.setPoolId(request.getConfig().getResourcePoolId());
            BaseSystemConfigDTO baseInfo = systemParameterService.getBaseInfo();
            runRequest.setPlatformUrl(GenerateHashTreeUtil.getPlatformUrl(baseInfo, runRequest, null));
        }
        // 加载自定义JAR
        Map<String, List<ProjectJarConfig>> loadJar = NewDriverManager.loadJar(request, runRequest.getPool());
        if (MapUtils.isNotEmpty(loadJar)) {
            TestPlan test = (TestPlan) runRequest.getHashTree().getArray()[0];
            test.setProperty(ApiTestConstants.JAR_PATH, JSON.toJSONString(loadJar.keySet().stream().toList()));
            runRequest.setCustomJarInfo(loadJar);
        }
        runRequest.setFakeErrorMap(ApiFakeErrorUtil.get(
                NewDriverManager.getProjectIds(request)));
        jMeterService.run(runRequest);
        return request.getId();
    }

    private void uploadBodyFiles(List<String> bodyFileRequestIds, List<MultipartFile> bodyFiles) {
        if (CollectionUtils.isNotEmpty(bodyFileRequestIds)) {
            bodyFileRequestIds.forEach(requestId -> {
                FileUtils.createBodyFiles(requestId, bodyFiles);
            });
        }
    }

    /**
     * 暂时保留给UI使用
     *
     * @param request
     * @param reportId
     * @param name
     * @param ids
     * @return
     */
    @Deprecated
    protected ApiScenarioReportResult getApiScenarioReportResult(RunScenarioRequest request, String reportId, StringBuilder name, String ids) {
        return apiScenarioReportService.getApiScenarioReportResult(request, reportId, name.toString(), ids);
    }
}

