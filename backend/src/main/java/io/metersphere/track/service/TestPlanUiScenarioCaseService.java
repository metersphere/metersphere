package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunTestPlanScenarioRequest;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.UiAutomationServiceProxy;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestPlanUiScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.dto.*;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.service.ProjectApplicationService;
import io.metersphere.service.ProjectService;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.testcase.TestPlanScenarioCaseBatchRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanUiScenarioCaseService {

    @Resource
    UiAutomationServiceProxy uiAutomationServiceProxy;
    @Resource
    TestPlanUiScenarioMapper testPlanUiScenarioMapper;
    @Resource
    ExtTestPlanUiScenarioCaseMapper extTestPlanUiScenarioCaseMapper;
    @Resource
    ApiScenarioReportService apiScenarioReportService;
    @Resource
    ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ProjectService projectService;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private UiScenarioMapper uiScenarioMapper;

    public List<UiScenarioDTO> list(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<UiScenarioDTO> apiTestCases = extTestPlanUiScenarioCaseMapper.list(request);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildProjectInfo(apiTestCases);
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public void buildProjectInfo(List<? extends UiScenarioDTO> apiTestCases) {
        List<String> projectIds = apiTestCases.stream()
                .map(UiScenarioDTO::getProjectId)
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toList());

        Map<String, Project> projectMap = projectService.getProjectByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, project -> project));

        apiTestCases.forEach(item -> {
            Project project = projectMap.get(item.getProjectId());
            if(project != null){
                ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.SCENARIO_CUSTOM_NUM.name());
                boolean custom = config.getScenarioCustomNum();
                if (custom) {
                    item.setCustomNum(item.getCustomNum());
                }else {
                    item.setCustomNum(item.getNum().toString());
                }
            }else {
                item.setCustomNum(item.getNum().toString());
            }
        });
    }

    public void buildUserInfo(List<? extends UiScenarioDTO> apiTestCases) {
        List<String> userIds = new ArrayList();
        userIds.addAll(apiTestCases.stream().map(UiScenarioDTO::getUserId).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(UiScenarioDTO::getPrincipal).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
            apiTestCases.forEach(caseResult -> {
                caseResult.setCreatorName(userMap.get(caseResult.getCreateUser()));
                caseResult.setPrincipalName(userMap.get(caseResult.getPrincipal()));
            });
        }
    }

    public List<String> selectIds(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<String> idList = extTestPlanUiScenarioCaseMapper.selectIds(request);
        return idList;
    }

    public Pager<List<UiScenarioDTO>> relevanceList(UiScenarioRequest request, int goPage, int pageSize) {
        request.setNotInTestPlan(true);
        if (testPlanService.isAllowedRepeatCase(request.getPlanId())) {
            request.setNotInTestPlan(false);
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, uiAutomationServiceProxy.list(request));
    }

    public int delete(String id) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria()
                .andIdEqualTo(id);

        return testPlanUiScenarioMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanScenarioCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria()
                .andIdIn(ids);
        testPlanUiScenarioMapper.deleteByExample(example);
    }

    public List<MsExecResponseDTO> run(RunTestPlanScenarioRequest testPlanScenarioRequest) {
        StringBuilder idStr = new StringBuilder();
        List<String> planCaseIdList = testPlanScenarioRequest.getPlanCaseIds();
        if (testPlanScenarioRequest.getCondition() != null && testPlanScenarioRequest.getCondition().isSelectAll()) {
            planCaseIdList = this.selectIds(testPlanScenarioRequest.getCondition());
            if (testPlanScenarioRequest.getCondition().getUnSelectIds() != null) {
                planCaseIdList.removeAll(testPlanScenarioRequest.getCondition().getUnSelectIds());
            }
        }
        testPlanScenarioRequest.setPlanCaseIds(planCaseIdList);
        if (CollectionUtils.isEmpty(planCaseIdList)) {
            MSException.throwException("未找到执行场景！");
        }
        RunModeConfigDTO config = testPlanScenarioRequest.getConfig();
        if (config != null) {
            String envType = config.getEnvironmentType();
            String envGroupId = config.getEnvironmentGroupId();
            Map<String, String> envMap = config.getEnvMap();
            if ((StringUtils.equals(envType, EnvironmentType.JSON.toString()) && envMap != null && !envMap.isEmpty())
                    || (StringUtils.equals(envType, EnvironmentType.GROUP.toString()) && StringUtils.isNotBlank(envGroupId))) {
                // 更新场景用例环境信息，运行时从数据库读取最新环境
                this.setScenarioEnv(new ArrayList<>(), planCaseIdList, testPlanScenarioRequest.getConfig());
            }
        }
        planCaseIdList.forEach(item -> {
            idStr.append("\"").append(item).append("\"").append(",");
        });
        List<TestPlanUiScenario> testPlanApiScenarioList = extTestPlanUiScenarioCaseMapper.selectByIds(idStr.toString().substring(0, idStr.toString().length() - 1), "\"" + StringUtils.join(testPlanScenarioRequest.getPlanCaseIds(), ",") + "\"");
        List<String> scenarioIds = new ArrayList<>();
        Map<String, String> scenarioPlanIdMap = new LinkedHashMap<>();
        for (TestPlanUiScenario apiScenario : testPlanApiScenarioList) {
            scenarioIds.add(apiScenario.getUiScenarioId());
            scenarioPlanIdMap.put(apiScenario.getId(), apiScenario.getUiScenarioId());
        }
        if (scenarioPlanIdMap.isEmpty()) {
            MSException.throwException("未找到执行场景！");
        }
        RunUiScenarioRequest request = new RunUiScenarioRequest();
        request.setIds(scenarioIds);
        request.setReportId(testPlanScenarioRequest.getId());
        request.setScenarioTestPlanIdMap(scenarioPlanIdMap);
        request.setRunMode(ApiRunMode.UI_SCENARIO_PLAN.name());
        request.setId(testPlanScenarioRequest.getId());
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(testPlanScenarioRequest.getTriggerMode());
        request.setConfig(testPlanScenarioRequest.getConfig());
        request.setProjectId(testPlanScenarioRequest.getProjectId());
        UiRunModeConfigDTO configDTO = new UiRunModeConfigDTO();
        BeanUtils.copyBean(configDTO, testPlanScenarioRequest.getConfig());
        request.setUiConfig(configDTO);
        request.setPlanCaseIds(planCaseIdList);
        request.setRequestOriginator("TEST_PLAN");
        return uiAutomationServiceProxy.run(request);
    }

    public void setScenarioEnv(List<TestPlanUiScenario> testPlanApiScenarios, List<String> planScenarioIds, RunModeConfigDTO runModeConfig) {
        if (CollectionUtils.isEmpty(planScenarioIds)) return;

        if (CollectionUtils.isEmpty(testPlanApiScenarios)) {
            TestPlanUiScenarioExample testPlanApiScenarioExample = new TestPlanUiScenarioExample();
            testPlanApiScenarioExample.createCriteria().andIdIn(planScenarioIds);
            testPlanApiScenarios = testPlanUiScenarioMapper.selectByExampleWithBLOBs(testPlanApiScenarioExample);
        }
        if (CollectionUtils.isEmpty(planScenarioIds)) {
            return;
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanUiScenarioMapper mapper = sqlSession.getMapper(TestPlanUiScenarioMapper.class);

        String environmentType = runModeConfig.getEnvironmentType();
        String environmentGroupId = runModeConfig.getEnvironmentGroupId();

        if (StringUtils.equals(environmentType, EnvironmentType.JSON.toString())) {
            Map<String, String> envMap = runModeConfig.getEnvMap();
            for (TestPlanUiScenario testPlanApiScenario : testPlanApiScenarios) {
                String env = testPlanApiScenario.getEnvironment();
                if (StringUtils.isBlank(env)) {
                    if (envMap != null && !envMap.isEmpty()) {
                        env = JSON.toJSONString(envMap);
                    }
                }
                Map<String, String> map = JSON.parseObject(env, Map.class);
                if (map.isEmpty()) {
                    continue;
                }
                Set<String> set = map.keySet();
                for (String s : set) {
                    if (StringUtils.isNotBlank(envMap.get(s))) {
                        map.put(s, envMap.get(s));
                    }
                }
                String envJsonStr = JSON.toJSONString(map);
                if (!StringUtils.equals(envJsonStr, testPlanApiScenario.getEnvironment())) {
                    testPlanApiScenario.setEnvironmentType(EnvironmentType.JSON.toString());
                    testPlanApiScenario.setEnvironment(JSON.toJSONString(map));
                    mapper.updateByPrimaryKeyWithBLOBs(testPlanApiScenario);
                }
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }

        if (StringUtils.equals(environmentType, EnvironmentType.GROUP.toString())) {
            for (TestPlanUiScenario testPlanApiScenario : testPlanApiScenarios) {
                testPlanApiScenario.setEnvironmentType(EnvironmentType.GROUP.toString());
                testPlanApiScenario.setEnvironmentGroupId(environmentGroupId);
                mapper.updateByPrimaryKeyWithBLOBs(testPlanApiScenario);
            }
            sqlSession.flushStatements();
        }

    }

    public List<TestPlanUiScenario> getCasesByPlanId(String planId) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanUiScenarioMapper.selectByExample(example);
    }

    public List<String> getExecResultByPlanId(String planId) {
        return extTestPlanUiScenarioCaseMapper.getExecResultByPlanId(planId);
    }

    public void deleteByPlanId(String planId) {
        TestPlanScenarioCaseBatchRequest request = new TestPlanScenarioCaseBatchRequest();
        List<String> ids = extTestPlanUiScenarioCaseMapper.getIdsByPlanId(planId);
        request.setIds(ids);
        deleteApiCaseBath(request);
    }

    public void deleteByRelevanceProjectIds(String planId, List<String> relevanceProjectIds) {
        TestPlanScenarioCaseBatchRequest request = new TestPlanScenarioCaseBatchRequest();
        request.setIds(extTestPlanUiScenarioCaseMapper.getNotRelevanceCaseIds(planId, relevanceProjectIds));
        request.setPlanId(planId);
        deleteApiCaseBath(request);
    }

    public void bathDeleteByScenarioIds(List<String> ids) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andUiScenarioIdIn(ids);
        testPlanUiScenarioMapper.deleteByExample(example);
    }

    public void deleteByScenarioId(String id) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andUiScenarioIdEqualTo(id);
        testPlanUiScenarioMapper.deleteByExample(example);
    }

    public void batchUpdateEnv(RelevanceScenarioRequest request) {
        Map<String, String> envMap = request.getEnvMap();
        String envType = request.getEnvironmentType();
        String envGroupId = request.getEnvGroupId();
        Map<String, List<String>> mapping = request.getMapping();
        Set<String> set = mapping.keySet();
        request.setIds(new ArrayList<>(set));
        if (set.isEmpty()) {
            return;
        }
        if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(envGroupId)) {
            set.forEach(id -> {
                TestPlanUiScenario scenario = new TestPlanUiScenario();
                scenario.setId(id);
                scenario.setEnvironmentType(EnvironmentType.GROUP.name());
                scenario.setEnvironmentGroupId(envGroupId);
                testPlanUiScenarioMapper.updateByPrimaryKeySelective(scenario);
            });
        } else if (StringUtils.equals(envType, EnvironmentType.JSON.name())) {
            set.forEach(id -> {
                Map<String, String> newEnvMap = new HashMap<>(16);
                if (envMap != null && !envMap.isEmpty()) {
                    List<String> list = mapping.get(id);
                    list.forEach(l -> {
                        newEnvMap.put(l, envMap.get(l));
                    });
                }
                if (!newEnvMap.isEmpty()) {
                    TestPlanUiScenario scenario = new TestPlanUiScenario();
                    scenario.setId(id);
                    scenario.setEnvironmentType(EnvironmentType.JSON.name());
                    scenario.setEnvironment(JSON.toJSONString(newEnvMap));
                    testPlanUiScenarioMapper.updateByPrimaryKeySelective(scenario);
                }
            });
        }

    }

    public List<UiScenarioDTO> selectAllTableRows(TestPlanScenarioCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        TestPlanScenarioRequest tableRequest = new TestPlanScenarioRequest();
        tableRequest.setIds(ids);
        return extTestPlanUiScenarioCaseMapper.list(tableRequest);
    }

    public String getLogDetails(String id) {
        TestPlanUiScenario scenario = testPlanUiScenarioMapper.selectByPrimaryKey(id);
        if (scenario != null) {
            ApiScenarioWithBLOBs testCase = apiScenarioMapper.selectByPrimaryKey(scenario.getUiScenarioId());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(scenario.getTestPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testPlan.getProjectId(), testCase.getName(), scenario.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanUiScenario> nodes = testPlanUiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(nodes)) {
            ApiScenarioExample scenarioExample = new ApiScenarioExample();
            scenarioExample.createCriteria().andIdIn(nodes.stream().map(TestPlanUiScenario::getUiScenarioId).collect(Collectors.toList()));
            List<ApiScenario> scenarios = apiScenarioMapper.selectByExample(scenarioExample);
            if (CollectionUtils.isNotEmpty(scenarios)) {
                List<String> names = scenarios.stream().map(ApiScenario::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), scenarios.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public TestPlanUiScenario get(String id) {
        return testPlanUiScenarioMapper.selectByPrimaryKey(id);
    }

    public Boolean hasFailCase(String planId, List<String> automationIds) {
        if (CollectionUtils.isEmpty(automationIds)) {
            return false;
        }
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andUiScenarioIdIn(automationIds)
                .andLastResultEqualTo("Fail");
        return testPlanUiScenarioMapper.countByExample(example) > 0 ? true : false;
    }

    public Map<String, String> getScenarioCaseEnv(Map<String, String> map) {
        Set<String> set = map.keySet();
        HashMap<String, String> envMap = new HashMap<>(16);
        if (set.isEmpty()) {
            return envMap;
        }
        for (String projectId : set) {
            String envId = map.get(projectId);
            if (StringUtils.isBlank(envId)) {
                continue;
            }
            Project project = projectService.getProjectById(projectId);
            ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentMapper.selectByPrimaryKey(envId);
            if (project == null || environment == null) {
                continue;
            }
            String projectName = project.getName();
            String envName = environment.getName();
            if (StringUtils.isBlank(projectName) || StringUtils.isBlank(envName)) {
                continue;
            }
            envMap.put(projectName, envName);
        }
        return envMap;
    }

    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = extTestPlanUiScenarioCaseMapper.selectForPlanReport(planId);
        calculatePlanReport(report, planReportCaseDTOS);
    }

    public void calculatePlanReport(List<String> reportIds, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = apiScenarioReportService.selectForPlanReport(reportIds);
        calculatePlanReport(report, planReportCaseDTOS);
    }

    private void calculatePlanReport(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanUiResultReportDTO uiResult = report.getUiResult();

        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();
        TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, "Success");
        TestPlanUtils.addToReportCommonStatusResultList(statusResultMap, statusResult);
        TestPlanScenarioStepCountDTO stepCount = new TestPlanScenarioStepCountDTO();
        for (PlanReportCaseDTO item : planReportCaseDTOS) {
            calculateScenarioResultDTO(item, stepCount);
        }
        int underwayStepsCounts = getUnderwayStepsCounts(stepCount.getUnderwayIds());
        List<TestCaseReportStatusResultDTO> stepResult = new ArrayList<>();
        getScenarioCaseReportStatusResultDTO(TestPlanTestCaseStatus.Failure.name(), stepCount.getScenarioStepError(), stepResult);
        getScenarioCaseReportStatusResultDTO(TestPlanTestCaseStatus.Pass.name(), stepCount.getScenarioStepSuccess(), stepResult);
        getScenarioCaseReportStatusResultDTO(ExecuteResult.ERROR_REPORT_RESULT.toString(), stepCount.getScenarioStepErrorReport(), stepResult);
        getScenarioCaseReportStatusResultDTO(TestPlanTestCaseStatus.Underway.name(),
                stepCount.getScenarioStepTotal() - stepCount.getScenarioStepSuccess() - stepCount.getScenarioStepError() - stepCount.getScenarioStepErrorReport() + underwayStepsCounts, stepResult);
        uiResult.setUiScenarioData(statusResult);
        uiResult.setUiScenarioStepData(stepResult);
        report.setUiResult(uiResult);
    }

    private int getUnderwayStepsCounts(List<String> underwayIds) {
        if (CollectionUtils.isNotEmpty(underwayIds)) {
            List<Integer> underwayStepsCounts = extTestPlanUiScenarioCaseMapper.getUnderwaySteps(underwayIds);
            return underwayStepsCounts.stream().filter(Objects::nonNull).reduce(0, Integer::sum);
        }
        return 0;
    }

    private void calculateScenarioResultDTO(PlanReportCaseDTO item,
                                            TestPlanScenarioStepCountDTO stepCount) {
        if (StringUtils.isNotBlank(item.getReportId())) {
            APIScenarioReportResult uiScenarioReportResult = apiScenarioReportService.get(item.getReportId(), false);

            if (uiScenarioReportResult != null) {
                String content = uiScenarioReportResult.getContent();
                if (StringUtils.isNotBlank(content)) {
                    JSONObject jsonObject = JSONObject.parseObject(content);

                    stepCount.setScenarioStepSuccess(stepCount.getScenarioStepSuccess() + jsonObject.getIntValue("scenarioStepSuccess"));
                    stepCount.setScenarioStepError(stepCount.getScenarioStepError() + jsonObject.getIntValue("scenarioStepError"));
                    stepCount.setScenarioStepErrorReport(stepCount.getScenarioStepErrorReport() + jsonObject.getIntValue("scenarioStepErrorReport"));

                    if (!StringUtils.equalsIgnoreCase("STOP", uiScenarioReportResult.getStatus())) {
                        stepCount.setScenarioStepTotal(stepCount.getScenarioStepTotal() + jsonObject.getIntValue("scenarioStepTotal"));
                        stepCount.setScenarioStepUnExecute(stepCount.getScenarioStepUnExecute() + jsonObject.getIntValue("scenarioStepUnExecuteReport"));
                    } else {
                        //串行执行的报告 勾选了失败停止 后续的所有场景的未禁用步骤都统计到总数和未执行里面去
                        UiScenarioWithBLOBs stoppedScenario = uiScenarioMapper.selectByPrimaryKey(uiScenarioReportResult.getScenarioId());
                        if (stoppedScenario == null) {
                            TestPlanUiScenario testPlanUiScenario = testPlanUiScenarioMapper.selectByPrimaryKey(uiScenarioReportResult.getScenarioId());
                            stoppedScenario = uiScenarioMapper.selectByPrimaryKey(testPlanUiScenario.getUiScenarioId());
                        }
                        if (stoppedScenario == null) {
                            return;
                        }
                        int totalSteps = getTotalSteps(stoppedScenario);
                        stepCount.setScenarioStepTotal(stepCount.getScenarioStepTotal() + totalSteps);
                        stepCount.setScenarioStepUnExecute(stepCount.getScenarioStepUnExecute() + totalSteps);
                    }
                }
            }
        } else {
            stepCount.getUnderwayIds().add(item.getCaseId());
        }
    }

    /**
     * 获取一个ui场景所有未禁用的步骤数
     *
     * @param stoppedScenario
     * @return
     */
    private int getTotalSteps(UiScenarioWithBLOBs stoppedScenario) {
        if (StringUtils.isNotBlank(stoppedScenario.getScenarioDefinition())) {
            JSONObject definition = JSONObject.parseObject(stoppedScenario.getScenarioDefinition());
            if (definition.containsKey("hashTree")) {
                return definition
                        .getJSONArray("hashTree")
                        .stream()
                        .filter(cmd -> (((JSONObject) cmd).getBoolean("enable")))
                        .collect(Collectors.toList())
                        .size();
            }
        }
        return 0;
    }

    private void getScenarioCaseReportStatusResultDTO(String status, int count, List<TestCaseReportStatusResultDTO> scenarioCaseList) {
        if (count > 0) {
            TestCaseReportStatusResultDTO scenarioCase = new TestCaseReportStatusResultDTO();
            scenarioCase.setStatus(status);
            scenarioCase.setCount(count);
            scenarioCaseList.add(scenarioCase);
        }
    }

    public List<TestPlanUiScenarioDTO> getAllCasesByStatusList(String planId, List<String> statusList) {
        List<TestPlanUiScenarioDTO> uiTestCases =
                extTestPlanUiScenarioCaseMapper.getPlanUiScenarioByStatusList(planId, statusList);
        return buildCases(uiTestCases);
    }

    public List<TestPlanUiScenarioDTO> getAllCases(Map<String, String> idMap, Map<String, TestPlanUiScenarioDTO> scenarioInfoDTOMap) {
        String defaultStatus = "Error";
        Map<String, String> reportStatus = apiScenarioReportService.getReportStatusByReportIds(idMap.values());
        Map<String, String> savedReportMap = new HashMap<>(idMap);
        List<TestPlanUiScenarioDTO> apiTestCases = new ArrayList<>();
        for (TestPlanUiScenarioDTO dto : scenarioInfoDTOMap.values()) {
            String reportId = savedReportMap.get(dto.getId());
            savedReportMap.remove(dto.getId());
            dto.setReportId(reportId);
            if (StringUtils.isNotEmpty(reportId)) {
                String status = reportStatus.get(reportId);
                if (status == null) {
                    status = defaultStatus;
                }
                dto.setLastResult(status);
                dto.setStatus(status);
            }
            apiTestCases.add(dto);
        }
        return buildCases(apiTestCases);
    }

    public List<TestPlanUiScenarioDTO> buildCases(List<TestPlanUiScenarioDTO> apiTestCases) {
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildProjectInfo(apiTestCases);
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public TestPlanUiScenario selectByReportId(String reportId) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<TestPlanUiScenario> testPlanApiScenarios = testPlanUiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(testPlanApiScenarios)) {
            return testPlanApiScenarios.get(0);
        }
        return null;
    }

    public String getProjectIdById(String testPlanScenarioId) {
        return extTestPlanUiScenarioCaseMapper.getProjectIdById(testPlanScenarioId);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestPlanUiScenario.class, TestPlanUiScenarioMapper.class,
                extTestPlanUiScenarioCaseMapper::selectPlanIds,
                extTestPlanUiScenarioCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestPlanUiScenario.class,
                testPlanUiScenarioMapper::selectByPrimaryKey,
                extTestPlanUiScenarioCaseMapper::getPreOrder,
                extTestPlanUiScenarioCaseMapper::getLastOrder,
                testPlanUiScenarioMapper::updateByPrimaryKeySelective);
    }

    public List<String> relevanceListIds(UiScenarioRequest request) {
        request.setNotInTestPlan(true);
        if (testPlanService.isAllowedRepeatCase(request.getPlanId())) {
            request.setNotInTestPlan(false);
        }
        return uiAutomationServiceProxy.list(request).stream().map(UiScenarioDTO::getId).collect(Collectors.toList());
    }
}
