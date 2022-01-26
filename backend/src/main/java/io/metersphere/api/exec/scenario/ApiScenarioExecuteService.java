package io.metersphere.api.exec.scenario;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.*;
import io.metersphere.base.domain.ApiScenarioExample;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ApiScenarioExecuteService {
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    @Lazy
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private RemakeReportService remakeReportService;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;
    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private ApiScenarioParallelService apiScenarioParallelService;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

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

        if (StringUtils.equals("GROUP", request.getConfig().getEnvironmentType()) && StringUtils.isNotEmpty(request.getConfig().getEnvironmentGroupId())) {
            request.getConfig().setEnvMap(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }

        // 生成集成报告
        String serialReportId = null;
        LoggerUtil.info("Scenario run-执行脚本装载-根据条件查询所有场景 ");
        List<ApiScenarioWithBLOBs> apiScenarios = this.get(request);
        // 只有一个场景且没有测试步骤，则提示
        if (apiScenarios != null && apiScenarios.size() == 1 && (apiScenarios.get(0).getStepTotal() == null || apiScenarios.get(0).getStepTotal() == 0)) {
            MSException.throwException((apiScenarios.get(0).getName() + "，" + Translator.get("automation_exec_info")));
        }
        // 环境检查
        LoggerUtil.info("Scenario run-执行脚本装载-开始针对所有执行场景进行环境检查");
        apiScenarioEnvService.checkEnv(request, apiScenarios);
        // 集合报告设置
        if (request.getConfig() != null && StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString())
                && StringUtils.isNotEmpty(request.getConfig().getReportName())) {
            if (request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                request.setExecuteType(ExecuteType.Completed.name());
            } else {
                request.setExecuteType(ExecuteType.Marge.name());
            }
            serialReportId = UUID.randomUUID().toString();
        }
        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();

        Map<String, RunModeDataDTO> executeQueue = new LinkedHashMap<>();
        List<String> scenarioIds = new ArrayList<>();
        StringBuilder scenarioNames = new StringBuilder();

        LoggerUtil.info("Scenario run-执行脚本装载-初始化执行队列");
        if (StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            //测试计划执行
            assemblyPlanScenario(apiScenarios, request, executeQueue, scenarioIds, scenarioNames);
        } else {
            // 按照场景执行
            assemblyScenario(apiScenarios, request, executeQueue, scenarioIds, scenarioNames, serialReportId);
        }
        LoggerUtil.info("Scenario run-执行脚本装载-初始化执行队列完成：" + executeQueue.size());

        if (GenerateHashTreeUtil.isSetReport(request.getConfig())) {
            LoggerUtil.info("Scenario run-执行脚本装载-初始化集成报告：" + serialReportId);
            request.getConfig().setReportId(UUID.randomUUID().toString());

            APIScenarioReportResult report = apiScenarioReportService.init(request.getConfig().getReportId(),
                    JSON.toJSONString(CollectionUtils.isNotEmpty(scenarioIds) && scenarioIds.size() > 50 ? scenarioIds.subList(0, 50) : scenarioIds),
                    scenarioNames.length() >= 3000 ? scenarioNames.substring(0, 2000) : scenarioNames.deleteCharAt(scenarioNames.length() - 1).toString(),
                    ReportTriggerMode.MANUAL.name(), ExecuteType.Saved.name(), request.getProjectId(), request.getReportUserID(), request.getConfig(), JSON.toJSONString(scenarioIds));
            report.setVersionId(apiScenarios.get(0).getVersionId());
            report.setName(request.getConfig().getReportName());
            report.setId(serialReportId);
            request.getConfig().setAmassReport(serialReportId);
            apiScenarioReportMapper.insert(report);
            responseDTOS.add(new MsExecResponseDTO(JSON.toJSONString(scenarioIds), serialReportId, request.getRunMode()));
            // 增加并行集合报告
            if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.PARALLEL.toString())) {
                apiScenarioReportStructureService.save(apiScenarios, serialReportId, request.getConfig() != null
                        ? request.getConfig().getReportType() : null);
            }
        }
        // 开始执行
        if (executeQueue != null && executeQueue.size() > 0) {
            String reportType = request.getConfig().getReportType();
            String planReportId = StringUtils.isNotEmpty(request.getTestPlanReportId()) ? request.getTestPlanReportId() : serialReportId;
            DBTestQueue executionQueue = apiExecutionQueueService.add(executeQueue, request.getConfig().getResourcePoolId()
                    , ApiRunMode.SCENARIO.name(), planReportId, reportType, request.getRunMode(), request.getConfig());

            if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                if (StringUtils.isNotEmpty(serialReportId)) {
                    apiScenarioReportStructureService.save(apiScenarios, serialReportId, request.getConfig() != null ? request.getConfig().getReportType() : null);
                }
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                ApiScenarioReportMapper batchMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
                // 非集合报告，先生成执行队列
                if (StringUtils.isEmpty(serialReportId)) {
                    for (String reportId : executeQueue.keySet()) {
                        APIScenarioReportResult report = executeQueue.get(reportId).getReport();
                        report.setStatus(APITestStatus.Waiting.name());
                        batchMapper.insert(report);
                        responseDTOS.add(new MsExecResponseDTO(executeQueue.get(reportId).getTestId(), reportId, request.getRunMode()));
                    }
                    sqlSession.flushStatements();
                    if (sqlSession != null && sqlSessionFactory != null) {
                        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                    }
                }
                if (executionQueue.getQueue() != null) {
                    apiScenarioSerialService.serial(executionQueue, executionQueue.getQueue());
                }
            } else {
                apiScenarioParallelService.parallel(executeQueue, request, serialReportId, responseDTOS, executionQueue);
            }
        }
        return responseDTOS;
    }

    private List<ApiScenarioWithBLOBs> get(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<String> ids = request.getIds();
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenarioWithBLOBs> apiScenarios = apiScenarioMapper.selectByExampleWithBLOBs(example);
        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            if (request.getCondition() == null || !request.getCondition().isSelectAll()) {
                // 按照id指定顺序排序
                FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(ids);
                fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
                BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
                Collections.sort(apiScenarios, beanComparator);
            }
        }
        return apiScenarios;
    }


    /**
     * 测试计划接口场景的预执行（生成场景报告）
     */
    private void assemblyPlanScenario(List<ApiScenarioWithBLOBs> apiScenarios, RunScenarioRequest request, Map<String, RunModeDataDTO> executeQueue, List<String> scenarioIds, StringBuilder scenarioNames) {
        String reportId = request.getId();
        Map<String, String> planScenarioIdMap = request.getScenarioTestPlanIdMap();
        if (MapUtils.isEmpty(planScenarioIdMap)) {
            return;
        }
        String projectId = request.getProjectId();
        Map<String, ApiScenarioWithBLOBs> scenarioMap = apiScenarios.stream().collect(Collectors.toMap(ApiScenarioWithBLOBs::getId, Function.identity(), (t1, t2) -> t1));
        for (Map.Entry<String, String> entry : planScenarioIdMap.entrySet()) {
            String testPlanScenarioId = entry.getKey();
            String scenarioId = entry.getValue();
            ApiScenarioWithBLOBs scenario = scenarioMap.get(scenarioId);

            if (scenario.getStepTotal() == null || scenario.getStepTotal() == 0) {
                continue;
            }
            APIScenarioReportResult report;
            Map<String, String> planEnvMap = new HashMap<>();

            //测试计划页面触发的执行方式，生成报告时createScenarioReport第二个参数需要特殊处理
            // 获取场景用例单独的执行环境
            TestPlanApiScenario planApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(testPlanScenarioId);
            String envJson = planApiScenario.getEnvironment();
            String envType = planApiScenario.getEnvironmentType();
            String envGroupId = planApiScenario.getEnvironmentGroupId();
            if (StringUtils.equals(envType, EnvironmentType.JSON.toString()) && StringUtils.isNotBlank(envJson)) {
                planEnvMap = JSON.parseObject(envJson, Map.class);
            } else if (StringUtils.equals(envType, EnvironmentType.GROUP.toString()) && StringUtils.isNotBlank(envGroupId)) {
                planEnvMap = environmentGroupProjectService.getEnvMap(envGroupId);
            }
            if (StringUtils.isEmpty(projectId)) {
                projectId = testPlanScenarioCaseService.getProjectIdById(testPlanScenarioId);
            }
            if (StringUtils.isEmpty(projectId)) {
                projectId = scenario.getProjectId();
            }
            report = apiScenarioReportService.init(reportId, testPlanScenarioId, scenario.getName(), request.getTriggerMode(),
                    request.getExecuteType(), projectId, request.getReportUserID(), request.getConfig(), scenario.getId());
            report.setVersionId(scenario.getVersionId());
            scenarioIds.add(scenario.getId());
            if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                RunModeDataDTO runModeDataDTO = new RunModeDataDTO();
                runModeDataDTO.setTestId(testPlanScenarioId);
                runModeDataDTO.setPlanEnvMap(planEnvMap);
                runModeDataDTO.setReport(report);
                runModeDataDTO.setReportId(report.getId());
                executeQueue.put(report.getId(), runModeDataDTO);
            } else {
                try {
                    // 生成并行报告和HashTree
                    RunModeDataDTO runModeDataDTO = new RunModeDataDTO(report, testPlanScenarioId);
                    if (request.getConfig() != null && !request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                        runModeDataDTO.setHashTree(GenerateHashTreeUtil.generateHashTree(scenario, reportId, planEnvMap, request.getConfig().getReportType()));
                    }
                    executeQueue.put(report.getId(), runModeDataDTO);
                } catch (Exception ex) {
                    scenarioIds.remove(scenario.getId());
                    executeQueue.remove(report.getId());
                    remakeReportService.remakeScenario(request.getRunMode(), testPlanScenarioId, scenario, report);
                    continue;
                }
            }
            scenarioNames.append(scenario.getName()).append(",");
            // 生成文档结构
            if (request.getConfig() == null || !StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString())) {
                apiScenarioReportStructureService.save(scenario, report.getId(), request.getConfig() != null ? request.getConfig().getReportType() : null);
            }
            // 重置报告ID
            reportId = UUID.randomUUID().toString();
        }
    }

    /**
     * 接口场景的预执行（生成场景报告）
     */
    private void assemblyScenario(List<ApiScenarioWithBLOBs> apiScenarios, RunScenarioRequest request, Map<String, RunModeDataDTO> executeQueue, List<String> scenarioIds, StringBuilder scenarioNames, String serialReportId) {
        String reportId = request.getId();
        for (int i = 0; i < apiScenarios.size(); i++) {
            ApiScenarioWithBLOBs item = apiScenarios.get(i);
            if (item.getStepTotal() == null || item.getStepTotal() == 0) {
                continue;
            }
            APIScenarioReportResult report = apiScenarioReportService.init(reportId, item.getId(), item.getName(), request.getTriggerMode(),
                    request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig(), item.getId());
            scenarioIds.add(item.getId());
            report.setVersionId(item.getVersionId());
            scenarioNames.append(item.getName()).append(",");
            if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                RunModeDataDTO runModeDataDTO = new RunModeDataDTO();
                runModeDataDTO.setTestId(item.getId());
                runModeDataDTO.setPlanEnvMap(new HashMap<>());
                if (request.getConfig().getEnvMap() != null) {
                    runModeDataDTO.setPlanEnvMap(request.getConfig().getEnvMap());
                }
                runModeDataDTO.setReport(report);
                runModeDataDTO.setReportId(report.getId());
                executeQueue.put(report.getId(), runModeDataDTO);
            } else {
                try {
                    // 生成报告和HashTree
                    RunModeDataDTO runModeDataDTO = new RunModeDataDTO(report, item.getId());
                    if (request.getConfig() != null && !request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                        HashTree hashTree = GenerateHashTreeUtil.generateHashTree(item, StringUtils.isNotEmpty(serialReportId) ? serialReportId + "-" + i : reportId, request.getConfig().getEnvMap(), request.getConfig().getReportType());
                        runModeDataDTO.setHashTree(hashTree);
                    }
                    runModeDataDTO.setPlanEnvMap(request.getConfig().getEnvMap());
                    executeQueue.put(report.getId(), runModeDataDTO);
                } catch (Exception ex) {
                    scenarioIds.remove(item.getId());
                    if (StringUtils.equalsAny(request.getTriggerMode(), TriggerMode.BATCH.name(), TriggerMode.SCHEDULE.name())) {
                        remakeReportService.remakeScenario(request.getRunMode(), null, item, report);
                    } else {
                        MSException.throwException(ex);
                    }
                }
            }
            // 生成报告结构
            if (request.getConfig() == null || !StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString())) {
                apiScenarioReportStructureService.save(item, report.getId(), request.getConfig() != null ? request.getConfig().getReportType() : null);
            }
            // 重置报告ID
            reportId = UUID.randomUUID().toString();
        }
    }

    public void testElement(RunDefinitionRequest request) {
        if (request.getTestElement() != null) {
            tcpApiParamService.checkTestElement(request.getTestElement());
        }
    }

    public String debug(RunDefinitionRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        Map<String, String> map = request.getEnvironmentMap();
        String envType = request.getEnvironmentType();
        if (StringUtils.equals(envType, EnvironmentType.GROUP.toString())) {
            String environmentGroupId = request.getEnvironmentGroupId();
            map = environmentGroupProjectService.getEnvMap(environmentGroupId);
        }
        ParameterConfig config = new ParameterConfig();
        if (map != null) {
            apiScenarioEnvService.setEnvConfig(map, config);
        }
        HashTree hashTree = null;
        try {
            this.testElement(request);
            hashTree = request.getTestElement().generateHashTree(config);
            LogUtil.info(request.getTestElement().getJmx(hashTree));
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
        }
        if (request.isSaved()) {
            APIScenarioReportResult report = apiScenarioReportService.init(request.getId(),
                    request.getScenarioId(),
                    request.getScenarioName(),
                    ReportTriggerMode.MANUAL.name(),
                    request.getExecuteType(),
                    request.getProjectId(),
                    SessionUtils.getUserId(),
                    request.getConfig(),
                    request.getId());
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(request.getScenarioId());
            String reportType = request.getConfig() != null ? request.getConfig().getReportType() : null;
            if (scenario != null) {
                report.setVersionId(scenario.getVersionId());
                apiScenarioReportStructureService.save(scenario, report.getId(), reportType);
            } else {
                if (request.getTestElement() != null && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree())) {
                    ApiScenarioWithBLOBs apiScenario = new ApiScenarioWithBLOBs();
                    apiScenario.setId(request.getScenarioId());
                    MsTestElement testElement = request.getTestElement().getHashTree().get(0).getHashTree().get(0);
                    if (testElement != null) {
                        apiScenario.setName(testElement.getName());
                        apiScenario.setScenarioDefinition(JSON.toJSONString(testElement));
                        apiScenarioReportStructureService.save(apiScenario, report.getId(), reportType);
                    }
                }
            }
            apiScenarioReportMapper.insert(report);
        }
        uploadBodyFiles(request.getBodyFileRequestIds(), bodyFiles);
        FileUtils.createBodyFiles(request.getScenarioFileIds(), scenarioFiles);
        String runMode = StringUtils.isEmpty(request.getRunMode()) ? ApiRunMode.SCENARIO.name() : request.getRunMode();
        // 调用执行方法
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(request.getId(), request.getId(), runMode, hashTree);
        runRequest.setDebug(true);
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
}

