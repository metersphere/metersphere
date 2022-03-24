package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.TestPlanUtils;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.dto.RunModeConfigDTO;
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
public class TestPlanScenarioCaseService {

    @Resource
    ApiAutomationService apiAutomationService;
    @Resource
    TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
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

    public List<ApiScenarioDTO> list(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<ApiScenarioDTO> apiTestCases = extTestPlanScenarioCaseMapper.list(request);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildProjectInfo(apiTestCases);
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public void buildProjectInfo(List<? extends ApiScenarioDTO> apiTestCases) {
        List<String> projectIds = apiTestCases.stream()
                .map(ApiScenarioDTO::getProjectId)
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toList());

        Map<String, Project> projectMap = projectService.getProjectByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, project -> project));

        apiTestCases.forEach(item -> {
            Project project = projectMap.get(item.getProjectId());
            ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.SCENARIO_CUSTOM_NUM.name());
            boolean custom = config.getCaseCustomNum();
            if (project != null && custom) {
                item.setCustomNum(item.getCustomNum());
            } else {
                item.setCustomNum(item.getNum().toString());
            }
        });
    }

    public void buildUserInfo(List<? extends ApiScenarioDTO> apiTestCases) {
        List<String> userIds = new ArrayList();
        userIds.addAll(apiTestCases.stream().map(ApiScenarioDTO::getUserId).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(ApiScenarioDTO::getPrincipal).collect(Collectors.toList()));
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
        List<String> idList = extTestPlanScenarioCaseMapper.selectIds(request);
        return idList;
    }

    public Pager<List<ApiScenarioDTO>> relevanceList(ApiScenarioRequest request, int goPage, int pageSize) {
        request.setNotInTestPlan(true);
        if (testPlanService.isAllowedRepeatCase(request.getPlanId())) {
            request.setNotInTestPlan(false);
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiAutomationService.list(request));
    }

    public int delete(String id) {
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(id);
        String reportId = testPlanApiScenario.getReportId();
        if (!StringUtils.isEmpty(reportId)) {
            apiScenarioReportService.delete(reportId);
        }
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria()
                .andIdEqualTo(id);

        return testPlanApiScenarioMapper.deleteByExample(example);
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
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria()
                .andIdIn(ids);
        List<String> reportIds = testPlanApiScenarioMapper.selectByExample(example).stream()
                .map(TestPlanApiScenario::getReportId).collect(Collectors.toList());
        apiScenarioReportService.deleteByIds(reportIds);
        testPlanApiScenarioMapper.deleteByExample(example);
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
                this.setScenarioEnv(planCaseIdList, testPlanScenarioRequest.getConfig());
            }
        }
        planCaseIdList.forEach(item -> {
            idStr.append("\"").append(item).append("\"").append(",");
        });
        List<TestPlanApiScenario> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectByIds(idStr.toString().substring(0, idStr.toString().length() - 1), "\"" + org.apache.commons.lang3.StringUtils.join(testPlanScenarioRequest.getPlanCaseIds(), ",") + "\"");
        List<String> scenarioIds = new ArrayList<>();
        Map<String, String> scenarioPlanIdMap = new LinkedHashMap<>();
        for (TestPlanApiScenario apiScenario : testPlanApiScenarioList) {
            scenarioIds.add(apiScenario.getApiScenarioId());
            scenarioPlanIdMap.put(apiScenario.getId(), apiScenario.getApiScenarioId());
        }
        if (scenarioPlanIdMap.isEmpty()) {
            MSException.throwException("未找到执行场景！");
        }
        RunScenarioRequest request = new RunScenarioRequest();
        request.setIds(scenarioIds);
        request.setReportId(testPlanScenarioRequest.getId());
        request.setScenarioTestPlanIdMap(scenarioPlanIdMap);
        request.setRunMode(ApiRunMode.SCENARIO_PLAN.name());
        request.setId(testPlanScenarioRequest.getId());
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(testPlanScenarioRequest.getTriggerMode());
        request.setConfig(testPlanScenarioRequest.getConfig());
        request.setPlanCaseIds(planCaseIdList);
        request.setRequestOriginator("TEST_PLAN");
        return apiAutomationService.run(request);
    }

    public void setScenarioEnv(List<String> planScenarioIds, RunModeConfigDTO runModeConfig) {
        if (CollectionUtils.isEmpty(planScenarioIds)) return;
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
        testPlanApiScenarioExample.createCriteria().andIdIn(planScenarioIds);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(testPlanApiScenarioExample);
        TestPlanApiScenarioMapper mapper = sqlSession.getMapper(TestPlanApiScenarioMapper.class);

        String environmentType = runModeConfig.getEnvironmentType();
        String environmentGroupId = runModeConfig.getEnvironmentGroupId();

        if (StringUtils.equals(environmentType, EnvironmentType.JSON.toString())) {
            Map<String, String> envMap = runModeConfig.getEnvMap();
            if (CollectionUtils.isEmpty(planScenarioIds)) {
                return;
            }
            for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
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
                testPlanApiScenario.setEnvironmentType(EnvironmentType.JSON.toString());
                testPlanApiScenario.setEnvironment(JSON.toJSONString(map));
                mapper.updateByPrimaryKeyWithBLOBs(testPlanApiScenario);
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
            return;
        }

        if (StringUtils.equals(environmentType, EnvironmentType.GROUP.toString())) {
            for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
                testPlanApiScenario.setEnvironmentType(EnvironmentType.GROUP.toString());
                testPlanApiScenario.setEnvironmentGroupId(environmentGroupId);
                mapper.updateByPrimaryKeyWithBLOBs(testPlanApiScenario);
            }
            sqlSession.flushStatements();
        }

    }

    public List<TestPlanApiScenario> getCasesByPlanId(String planId) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanApiScenarioMapper.selectByExample(example);
    }

    public List<String> getExecResultByPlanId(String planId) {
        return extTestPlanScenarioCaseMapper.getExecResultByPlanId(planId);
    }

    public void deleteByPlanId(String planId) {
        TestPlanScenarioCaseBatchRequest request = new TestPlanScenarioCaseBatchRequest();
        List<String> ids = extTestPlanScenarioCaseMapper.getIdsByPlanId(planId);
        request.setIds(ids);
        deleteApiCaseBath(request);
    }

    public void deleteByRelevanceProjectIds(String planId, List<String> relevanceProjectIds) {
        TestPlanScenarioCaseBatchRequest request = new TestPlanScenarioCaseBatchRequest();
        request.setIds(extTestPlanScenarioCaseMapper.getNotRelevanceCaseIds(planId, relevanceProjectIds));
        request.setPlanId(planId);
        deleteApiCaseBath(request);
    }

    public void bathDeleteByScenarioIds(List<String> ids) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andApiScenarioIdIn(ids);
        testPlanApiScenarioMapper.deleteByExample(example);
    }

    public void deleteByScenarioId(String id) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andApiScenarioIdEqualTo(id);
        testPlanApiScenarioMapper.deleteByExample(example);
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
                TestPlanApiScenario scenario = new TestPlanApiScenario();
                scenario.setId(id);
                scenario.setEnvironmentType(EnvironmentType.GROUP.name());
                scenario.setEnvironmentGroupId(envGroupId);
                testPlanApiScenarioMapper.updateByPrimaryKeySelective(scenario);
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
                    TestPlanApiScenario scenario = new TestPlanApiScenario();
                    scenario.setId(id);
                    scenario.setEnvironmentType(EnvironmentType.JSON.name());
                    scenario.setEnvironment(JSON.toJSONString(newEnvMap));
                    testPlanApiScenarioMapper.updateByPrimaryKeySelective(scenario);
                }
            });
        }

    }

    public List<ApiScenarioDTO> selectAllTableRows(TestPlanScenarioCaseBatchRequest request) {
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
        return extTestPlanScenarioCaseMapper.list(tableRequest);
    }

    public String getLogDetails(String id) {
        TestPlanApiScenario scenario = testPlanApiScenarioMapper.selectByPrimaryKey(id);
        if (scenario != null) {
            ApiScenarioWithBLOBs testCase = apiScenarioMapper.selectByPrimaryKey(scenario.getApiScenarioId());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(scenario.getTestPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testPlan.getProjectId(), testCase.getName(), scenario.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanApiScenario> nodes = testPlanApiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(nodes)) {
            ApiScenarioExample scenarioExample = new ApiScenarioExample();
            scenarioExample.createCriteria().andIdIn(nodes.stream().map(TestPlanApiScenario::getApiScenarioId).collect(Collectors.toList()));
            List<ApiScenario> scenarios = apiScenarioMapper.selectByExample(scenarioExample);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(scenarios)) {
                List<String> names = scenarios.stream().map(ApiScenario::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), scenarios.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public TestPlanApiScenario get(String id) {
        return testPlanApiScenarioMapper.selectByPrimaryKey(id);
    }

    public Boolean hasFailCase(String planId, List<String> automationIds) {
        if (CollectionUtils.isEmpty(automationIds)) {
            return false;
        }
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andApiScenarioIdIn(automationIds)
                .andLastResultEqualTo("Fail");
        return testPlanApiScenarioMapper.countByExample(example) > 0 ? true : false;
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
        List<PlanReportCaseDTO> planReportCaseDTOS = extTestPlanScenarioCaseMapper.selectForPlanReport(planId);
        calculatePlanReport(report, planReportCaseDTOS);
    }

    public void calculatePlanReport(List<String> reportIds, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = apiScenarioReportService.selectForPlanReport(reportIds);
        calculatePlanReport(report, planReportCaseDTOS);
    }

    private void calculatePlanReport(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanApiResultReportDTO apiResult = report.getApiResult();

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
        getScenarioCaseReportStatusResultDTO(ExecuteResult.errorReportResult.name(), stepCount.getScenarioStepErrorReport(), stepResult);
        getScenarioCaseReportStatusResultDTO(TestPlanTestCaseStatus.Underway.name(),
                stepCount.getScenarioStepTotal() - stepCount.getScenarioStepSuccess() - stepCount.getScenarioStepError() - stepCount.getScenarioStepErrorReport() + underwayStepsCounts, stepResult);
        apiResult.setApiScenarioData(statusResult);
        apiResult.setApiScenarioStepData(stepResult);
    }

    private int getUnderwayStepsCounts(List<String> underwayIds) {
        if (CollectionUtils.isNotEmpty(underwayIds)) {
            List<Integer> underwayStepsCounts = extTestPlanScenarioCaseMapper.getUnderwaySteps(underwayIds);
            return underwayStepsCounts.stream().filter(Objects::nonNull).reduce(0,Integer::sum);
        }
        return 0;
    }

    private void calculateScenarioResultDTO(PlanReportCaseDTO item,
                                            TestPlanScenarioStepCountDTO stepCount) {
        if (StringUtils.isNotBlank(item.getReportId())) {
            APIScenarioReportResult apiScenarioReportResult = apiScenarioReportService.get(item.getReportId());
            if (apiScenarioReportResult != null) {
                String content = apiScenarioReportResult.getContent();
                if (StringUtils.isNotBlank(content)) {
                    JSONObject jsonObject = JSONObject.parseObject(content);
                    stepCount.setScenarioStepTotal(stepCount.getScenarioStepTotal() + jsonObject.getIntValue("scenarioStepTotal"));
                    stepCount.setScenarioStepSuccess(stepCount.getScenarioStepSuccess() + jsonObject.getIntValue("scenarioStepSuccess"));
                    stepCount.setScenarioStepError(stepCount.getScenarioStepError() + jsonObject.getIntValue("scenarioStepError"));
                    stepCount.setScenarioStepErrorReport(stepCount.getScenarioStepErrorReport() + jsonObject.getIntValue("scenarioStepErrorReport"));
                    stepCount.setScenarioStepUnExecute(stepCount.getScenarioStepUnExecute() + jsonObject.getIntValue("scenarioStepUnExecuteReport"));
                }
            }
        } else {
            stepCount.getUnderwayIds().add(item.getCaseId());
        }
    }

    private void getScenarioCaseReportStatusResultDTO(String status, int count, List<TestCaseReportStatusResultDTO> scenarioCaseList) {
        if (count > 0) {
            TestCaseReportStatusResultDTO scenarioCase = new TestCaseReportStatusResultDTO();
            scenarioCase.setStatus(status);
            scenarioCase.setCount(count);
            scenarioCaseList.add(scenarioCase);
        }
    }

    public List<TestPlanFailureScenarioDTO> getAllCases(String planId) {
        List<TestPlanFailureScenarioDTO> apiTestCases =
                extTestPlanScenarioCaseMapper.getFailureList(planId, null);
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureScenarioDTO> getAllCases(Map<String, String> idMap) {
        List<TestPlanFailureScenarioDTO> apiTestCases =
                extTestPlanScenarioCaseMapper.getFailureListByIds(idMap.keySet(), null);

        String defaultStatus = "Fail";
        Map<String, String> reportStatus = apiScenarioReportService.getReportStatusByReportIds(idMap.values());
        for (TestPlanFailureScenarioDTO dto : apiTestCases) {
            String reportId = idMap.get(dto.getId());
            dto.setReportId(reportId);
            if (reportId != null) {
                String status = reportStatus.get(reportId);
                if (status == null) {
                    status = defaultStatus;
                } else {
                    if (StringUtils.equalsIgnoreCase(status, "Error")) {
                        status = "Fail";
                    }
                }
                dto.setLastResult(status);
                dto.setStatus(status);
            }
        }
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureScenarioDTO> getFailureCases(String planId) {
        List<TestPlanFailureScenarioDTO> apiTestCases =
                extTestPlanScenarioCaseMapper.getFailureList(planId, "Fail");
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureScenarioDTO> buildCases(List<TestPlanFailureScenarioDTO> apiTestCases) {
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildProjectInfo(apiTestCases);
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public TestPlanApiScenario selectByReportId(String reportId) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(testPlanApiScenarios)) {
            return testPlanApiScenarios.get(0);
        }
        return null;
    }

    public String getProjectIdById(String testPlanScenarioId) {
        return extTestPlanScenarioCaseMapper.getProjectIdById(testPlanScenarioId);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestPlanApiScenario.class, TestPlanApiScenarioMapper.class,
                extTestPlanScenarioCaseMapper::selectPlanIds,
                extTestPlanScenarioCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestPlanApiScenario.class,
                testPlanApiScenarioMapper::selectByPrimaryKey,
                extTestPlanScenarioCaseMapper::getPreOrder,
                extTestPlanScenarioCaseMapper::getLastOrder,
                testPlanApiScenarioMapper::updateByPrimaryKeySelective);
    }

    public List<TestPlanFailureScenarioDTO> getErrorReportCases(String planId) {
        List<TestPlanFailureScenarioDTO> apiTestCases =
                extTestPlanScenarioCaseMapper.getFailureList(planId, ExecuteResult.errorReportResult.name());
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureScenarioDTO> getUnExecuteCases(String planId) {
        List<TestPlanFailureScenarioDTO> apiTestCases =
                extTestPlanScenarioCaseMapper.getFailureList(planId, "unExecute");
        return buildCases(apiTestCases);
    }
}
