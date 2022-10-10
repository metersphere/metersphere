package io.metersphere.service.plan;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiCaseRelevanceRequest;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.RelevanceScenarioRequest;
import io.metersphere.api.dto.ScenarioEnv;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.automation.RunTestPlanScenarioRequest;
import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.api.dto.plan.ApiPlanReportDTO;
import io.metersphere.api.dto.plan.ApiPlanReportRequest;
import io.metersphere.api.dto.plan.TestPlanApiCaseInfoDTO;
import io.metersphere.api.dto.plan.TestPlanApiReportInfoDTO;
import io.metersphere.api.dto.plan.TestPlanApiScenarioInfoDTO;
import io.metersphere.api.dto.plan.TestPlanEnvInfoDTO;
import io.metersphere.api.dto.plan.TestPlanExecuteReportDTO;
import io.metersphere.api.dto.plan.TestPlanReportRunInfoDTO;
import io.metersphere.api.dto.plan.TestPlanScenarioCaseBatchRequest;
import io.metersphere.api.dto.plan.TestPlanScenarioStepCountDTO;
import io.metersphere.api.dto.plan.TestPlanScenarioStepCountSimpleDTO;
import io.metersphere.service.scenario.ApiScenarioService;
import io.metersphere.service.definition.ApiDefinitionExecResultService;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.scenario.ApiScenarioModuleService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioExample;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.domain.TestPlanApiScenarioExample;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioModuleMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiScenarioMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.ServiceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanScenarioCaseService {

    @Resource
    ApiScenarioService apiAutomationService;
    @Resource
    TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    ApiScenarioReportService apiScenarioReportService;
    @Resource
    BaseUserService baseUserService;
    @Resource
    ApiScenarioMapper apiScenarioMapper;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private BaseEnvGroupProjectService environmentGroupProjectService;
    @Resource
    private BaseEnvironmentService apiTestEnvironmentService;
    @Resource
    private ExtApiScenarioModuleMapper extApiScenarioModuleMapper;
    @Lazy
    @Resource
    private ApiScenarioModuleService apiScenarioModuleService;

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

        Map<String, Project> projectMap = baseProjectService.getProjectByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, project -> project));

        apiTestCases.forEach(item -> {
            Project project = projectMap.get(item.getProjectId());
            if (project != null) {
                ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.SCENARIO_CUSTOM_NUM.name());
                boolean custom = config.getScenarioCustomNum();
                if (custom) {
                    item.setCustomNum(item.getCustomNum());
                } else {
                    item.setCustomNum(item.getNum().toString());
                }
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
        if (request.getAllowedRepeatCase()) {
            request.setNotInTestPlan(false);
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiAutomationService.list(request));
    }

    public void relevance(ApiCaseRelevanceRequest request) {
        if (request.getAllowedRepeatCase()) {
            request.getCondition().setNotInTestPlan(false);
        } else {
            request.getCondition().setNotInTestPlan(true);
        }

        apiAutomationService.buildApiCaseRelevanceRequest(request);

        Map<String, List<String>> mapping = request.getMapping();
        Map<String, String> envMap = request.getEnvMap();
        Set<String> set = mapping.keySet();
        List<String> relevanceIds = request.getIds();
        Collections.reverse(relevanceIds);
        String envType = request.getEnvironmentType();
        String envGroupId = request.getEnvGroupId();
        if (set.isEmpty()) {
            return;
        }
        Long nextOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanScenarioCaseMapper::getLastOrder);
        for (String id : relevanceIds) {
            Map<String, String> newEnvMap = new HashMap<>(16);
            List<String> list = mapping.get(id);
            if (CollectionUtils.isEmpty(list)) {
                ScenarioEnv scenarioEnv = apiAutomationService.getApiScenarioProjectId(id);
                list = new ArrayList<>(scenarioEnv.getProjectIds());
            }
            list.forEach(l -> newEnvMap.put(l, envMap == null ? "" : envMap.getOrDefault(l, "")));
            TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
            testPlanApiScenario.setId(UUID.randomUUID().toString());
            testPlanApiScenario.setCreateUser(SessionUtils.getUserId());
            testPlanApiScenario.setApiScenarioId(id);
            testPlanApiScenario.setTestPlanId(request.getPlanId());
            testPlanApiScenario.setCreateTime(System.currentTimeMillis());
            testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
            String environmentJson = JSON.toJSONString(newEnvMap);
            if (StringUtils.equals(envType, EnvironmentType.JSON.name())) {
                testPlanApiScenario.setEnvironment(environmentJson);
                testPlanApiScenario.setEnvironmentType(EnvironmentType.JSON.name());
            } else if (StringUtils.equals(envType, EnvironmentType.GROUP.name())) {
                testPlanApiScenario.setEnvironmentType(EnvironmentType.GROUP.name());
                testPlanApiScenario.setEnvironmentGroupId(envGroupId);
                // JSON类型环境中也保存最新值
                testPlanApiScenario.setEnvironment(environmentJson);
            }
            testPlanApiScenario.setOrder(nextOrder);
            nextOrder += ServiceUtils.ORDER_STEP;
            testPlanApiScenarioMapper.insert(testPlanApiScenario);
        }
    }

    public int delete(String id) {
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
                this.setScenarioEnv(new ArrayList<>(), planCaseIdList, testPlanScenarioRequest.getConfig());
            }
        }
        planCaseIdList.forEach(item -> {
            idStr.append("\"").append(item).append("\"").append(",");
        });
        List<TestPlanApiScenario> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectByIds(idStr.toString().substring(0, idStr.toString().length() - 1), "\"" + StringUtils.join(testPlanScenarioRequest.getPlanCaseIds(), ",") + "\"");
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

    public RunModeConfigDTO setScenarioEnv(RunModeConfigDTO runModeConfig, String planId) {
        TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
        scenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(scenarioExample);
        List<String> planScenarioIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getId).collect(Collectors.toList());
        setScenarioEnv(testPlanApiScenarios, planScenarioIds, runModeConfig);
        return runModeConfig;
    }

    public void setScenarioEnv(List<TestPlanApiScenario> testPlanApiScenarios, List<String> planScenarioIds, RunModeConfigDTO runModeConfig) {
        if (CollectionUtils.isEmpty(planScenarioIds)) return;

        if (CollectionUtils.isEmpty(testPlanApiScenarios)) {
            TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
            testPlanApiScenarioExample.createCriteria().andIdIn(planScenarioIds);
            testPlanApiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(testPlanApiScenarioExample);
        }
        if (CollectionUtils.isEmpty(planScenarioIds)) {
            return;
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiScenarioMapper mapper = sqlSession.getMapper(TestPlanApiScenarioMapper.class);

        String environmentType = runModeConfig.getEnvironmentType();
        String environmentGroupId = runModeConfig.getEnvironmentGroupId();

        if (StringUtils.equals(environmentType, EnvironmentType.JSON.toString())) {
            Map<String, String> envMap = runModeConfig.getEnvMap();
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
            // todo check
//            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(scenario.getTestPlanId());
//            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testPlan.getProjectId(), testCase.getName(), scenario.getCreateUser(), new LinkedList<>());
//            return JSON.toJSONString(details);
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
            if (CollectionUtils.isNotEmpty(scenarios)) {
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
                .andLastResultEqualTo(ApiReportStatus.ERROR.name());
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
            Project project = baseProjectService.getProjectById(projectId);
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

    public List<TestPlanFailureScenarioDTO> getAllCases(String planId) {
        List<TestPlanFailureScenarioDTO> apiTestCases =
                extTestPlanScenarioCaseMapper.getFailureList(planId, null);
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureScenarioDTO> getAllCases(Map<String, String> idMap, Map<String, TestPlanFailureScenarioDTO> scenarioInfoDTOMap) {
        Map<String, String> reportStatus = apiScenarioReportService.getReportStatusByReportIds(idMap.values());
        Map<String, String> savedReportMap = new HashMap<>(idMap);
        List<TestPlanFailureScenarioDTO> apiTestCases = new ArrayList<>();
        for (TestPlanFailureScenarioDTO dto : scenarioInfoDTOMap.values()) {
            String reportId = savedReportMap.get(dto.getId());
            savedReportMap.remove(dto.getId());
            dto.setReportId(reportId);
            if (StringUtils.isNotEmpty(reportId)) {
                String status = reportStatus.get(reportId);
                if (status == null) {
                    status = ApiReportStatus.ERROR.name();
                }
                dto.setLastResult(status);
                dto.setStatus(status);
            }
            apiTestCases.add(dto);
        }
        return buildCases(apiTestCases);
    }


    public List<TestPlanFailureScenarioDTO> getFailureCases(String planId) {
        List<TestPlanFailureScenarioDTO> apiTestCases =
                extTestPlanScenarioCaseMapper.getFailureList(planId, ApiReportStatus.ERROR.name());
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
                extTestPlanScenarioCaseMapper.getFailureList(planId, ApiReportStatus.FAKE_ERROR.name());
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureScenarioDTO> getUnExecuteCases(String planId) {
        List<TestPlanFailureScenarioDTO> apiTestCases =
                extTestPlanScenarioCaseMapper.getFailureList(planId, ApiReportStatus.PENDING.name());
        return buildCases(apiTestCases);
    }

    public TestPlanScenarioStepCountSimpleDTO getStepCount(List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanScenarioStepCountDTO stepCount = new TestPlanScenarioStepCountDTO();
        for (PlanReportCaseDTO item : planReportCaseDTOS) {
            calculateScenarioResultDTO(item, stepCount);
        }
        int underwayStepsCounts = getUnderwayStepsCounts(stepCount.getUnderwayIds());
        TestPlanScenarioStepCountSimpleDTO stepResult = new TestPlanScenarioStepCountSimpleDTO();
        stepResult.setStepCount(stepCount);
        stepResult.setUnderwayStepsCounts(underwayStepsCounts);
        return stepResult;
    }

    private int getUnderwayStepsCounts(List<String> underwayIds) {
        if (CollectionUtils.isNotEmpty(underwayIds)) {
            List<Integer> underwayStepsCounts = extTestPlanScenarioCaseMapper.getUnderwaySteps(underwayIds);
            return underwayStepsCounts.stream().filter(Objects::nonNull).reduce(0, Integer::sum);
        }
        return 0;
    }

    private void calculateScenarioResultDTO(PlanReportCaseDTO item,
                                            TestPlanScenarioStepCountDTO stepCount) {
        if (StringUtils.isNotBlank(item.getReportId())) {
            ApiScenarioReportResult apiScenarioReportResult = apiScenarioReportService.get(item.getReportId(), false);
            if (apiScenarioReportResult != null) {
                String content = apiScenarioReportResult.getContent();
                if (StringUtils.isNotBlank(content)) {
                    Map map = JSON.parseMap(content);
                    stepCount.setScenarioStepTotal(stepCount.getScenarioStepTotal() + (Integer) map.get("scenarioStepTotal"));
                    stepCount.setScenarioStepSuccess(stepCount.getScenarioStepSuccess() + (Integer) map.get("scenarioStepSuccess"));
                    stepCount.setScenarioStepError(stepCount.getScenarioStepError() + (Integer) map.get("scenarioStepError"));
                    stepCount.setScenarioStepErrorReport(stepCount.getScenarioStepErrorReport() + (Integer) map.get("scenarioStepErrorReport"));
                    stepCount.setScenarioStepUnExecute(stepCount.getScenarioStepUnExecute() + (map.get("pending") == null ? 0 : (Integer) map.get("pending")));
                }
            }
        } else {
            stepCount.getUnderwayIds().add(item.getCaseId());
        }
    }

    public void relevanceByTestIds(List<String> ids, String planId) {
        Long nextScenarioOrder = ServiceUtils.getNextOrder(planId, extTestPlanScenarioCaseMapper::getLastOrder);
        for (String id : ids) {
            ApiScenarioWithBLOBs testPlanApiScenario = apiScenarioMapper.selectByPrimaryKey(id);
            if (testPlanApiScenario != null) {
                TestPlanApiScenario t = new TestPlanApiScenario();
                t.setId(UUID.randomUUID().toString());
                t.setTestPlanId(planId);
                t.setApiScenarioId(id);
                t.setPassRate(testPlanApiScenario.getPassRate());
                t.setReportId(testPlanApiScenario.getReportId());
                t.setStatus(testPlanApiScenario.getStatus());
                t.setCreateTime(System.currentTimeMillis());
                t.setUpdateTime(System.currentTimeMillis());
                t.setOrder(nextScenarioOrder);
                t.setEnvironmentType(testPlanApiScenario.getEnvironmentType());
                t.setEnvironment(testPlanApiScenario.getEnvironmentJson());
                t.setEnvironmentGroupId(testPlanApiScenario.getEnvironmentGroupId());
                nextScenarioOrder += 5000;
                TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
                example.createCriteria().andTestPlanIdEqualTo(planId).andApiScenarioIdEqualTo(t.getApiScenarioId());
                if (testPlanApiScenarioMapper.countByExample(example) <= 0) {
                    testPlanApiScenarioMapper.insert(t);
                }
            }
        }
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
            testPlanApiScenarioExample.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanApiScenario> apiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(testPlanApiScenarioExample);
            TestPlanApiScenarioMapper apiScenarioMapper = sqlSession.getMapper(TestPlanApiScenarioMapper.class);
            if (!CollectionUtils.isEmpty(apiScenarios)) {
                Long nextScenarioOrder = ServiceUtils.getNextOrder(targetPlanId, extTestPlanScenarioCaseMapper::getLastOrder);
                for (TestPlanApiScenario apiScenario : apiScenarios) {
                    TestPlanApiScenario planScenario = new TestPlanApiScenario();
                    planScenario.setId(UUID.randomUUID().toString());
                    planScenario.setTestPlanId(targetPlanId);
                    planScenario.setApiScenarioId(apiScenario.getApiScenarioId());
                    planScenario.setEnvironment(apiScenario.getEnvironment());
                    if (apiScenario.getEnvironmentType() != null) {
                        planScenario.setEnvironmentType(apiScenario.getEnvironmentType());
                    }
                    planScenario.setCreateTime(System.currentTimeMillis());
                    planScenario.setUpdateTime(System.currentTimeMillis());
                    planScenario.setCreateUser(SessionUtils.getUserId());
                    planScenario.setOrder(nextScenarioOrder);
                    nextScenarioOrder += 5000;
                    apiScenarioMapper.insert(planScenario);
                }
            }
            sqlSession.flushStatements();
        }
    }

    public boolean haveExecCase(String planId) {
        List<TestPlanApiScenario> testPlanApiScenarios = extTestPlanApiScenarioMapper.selectPlanByIdsAndStatusIsNotTrash(Arrays.asList(planId));
        return CollectionUtils.isNotEmpty(testPlanApiScenarios);
    }

    public Map<String, List<String>> getApiScenarioEnv(String planId) {
        TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
        scenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExample(scenarioExample);
        List<String> scenarioIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getId).collect(Collectors.toList());
        return this.getApiScenarioEnv(scenarioIds);
    }

    public Map<String, List<String>> getApiScenarioEnv(List<String> planApiScenarioIds) {
        Map<String, List<String>> envMap = new HashMap<>();
        if (CollectionUtils.isEmpty(planApiScenarioIds)) {
            return envMap;
        }

        TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
        scenarioExample.createCriteria().andIdIn(planApiScenarioIds);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(scenarioExample);

        for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
            String env = testPlanApiScenario.getEnvironment();
            if (StringUtils.isBlank(env)) {
                continue;
            }
            Map<String, String> map = JSON.parseObject(env, Map.class);
            if (!map.isEmpty()) {
                Set<String> set = map.keySet();
                for (String s : set) {
                    String e = map.get(s);
                    if (envMap.containsKey(s)) {
                        List<String> list = envMap.get(s);
                        if (!list.contains(e)) {
                            list.add(e);
                        }
                    } else {
                        List<String> envs = new ArrayList<>();
                        envs.add(e);
                        envMap.put(s, envs);
                    }
                }
            }
        }
        return envMap;
    }

    public ApiPlanReportDTO buildApiReport(ApiPlanReportRequest request) {
        ApiPlanReportDTO report = new ApiPlanReportDTO();
        Map config = request.getConfig();
        String planId = request.getPlanId();
        Boolean saveResponse = request.getSaveResponse();
        if (ServiceUtils.checkConfigEnable(config, "api")) {
            List<TestPlanFailureApiDTO> apiAllCases = null;
            List<TestPlanFailureScenarioDTO> scenarioAllCases = null;
            if (checkReportConfig(config, "api", "all")) {
                // 接口
                apiAllCases = testPlanApiCaseService.getAllCases(planId);
                report.setApiAllCases(apiAllCases);
                if (saveResponse) {
                    buildApiResponse(apiAllCases);
                }
                //场景
                scenarioAllCases = getAllCases(planId);
                if (saveResponse) {
                    buildScenarioResponse(scenarioAllCases);
                }
                report.setScenarioAllCases(scenarioAllCases);
            }
            //筛选符合配置需要的执行结果的用例和场景
            this.screenApiCaseByStatusAndReportConfig(report, apiAllCases, config);
            this.screenScenariosByStatusAndReportConfig(report, scenarioAllCases, config);
        }
        return report;
    }

    public void buildScenarioResponse(List<TestPlanFailureScenarioDTO> cases) {
        if (!CollectionUtils.isEmpty(cases)) {
            cases.forEach((item) -> {
                item.setResponse(apiScenarioReportService.get(item.getReportId(), true));
            });
        }
    }

    public void buildApiResponse(List<TestPlanFailureApiDTO> cases) {
        if (!CollectionUtils.isEmpty(cases)) {
            List<String> reportIds = new ArrayList<>();
            for (TestPlanFailureApiDTO apiCase : cases) {
                if (StringUtils.isEmpty(apiCase.getReportId())) {
                    ApiDefinitionExecResultWithBLOBs result = extApiDefinitionExecResultMapper.selectPlanApiMaxResultByTestIdAndType(apiCase.getId(), "API_PLAN");
                    if (result != null && StringUtils.isNotBlank(result.getContent())) {
                        apiCase.setReportId(result.getId());
                        String contentStr = result.getContent();
                        try {
                            Map content = JSON.parseMap(contentStr);
                            if (StringUtils.isNotEmpty(contentStr)) {
                                content.put("envName", apiDefinitionService.getEnvNameByEnvConfig(result.getProjectId(), result.getEnvConfig()));
                            }
                            contentStr = content.toString();
                            apiCase.setResponse(contentStr);
                        } catch (Exception e) {
                            LogUtil.error("解析content失败!", e);
                        }
                    }
                } else {
                    reportIds.add(apiCase.getReportId());
                }
            }
            if (CollectionUtils.isNotEmpty(reportIds)) {
                ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
                example.createCriteria().andIdIn(reportIds);
                List<ApiDefinitionExecResultWithBLOBs> results = apiDefinitionExecResultMapper.selectByExampleWithBLOBs(example);
                // 格式化数据结果
                Map<String, ApiDefinitionExecResultWithBLOBs> resultMap = results.stream().collect(Collectors.toMap(ApiDefinitionExecResult::getId, item -> item, (k, v) -> k));
                cases.forEach(item -> {
                    if (resultMap.get(item.getReportId()) != null &&
                            StringUtils.isNotBlank(resultMap.get(item.getReportId()).getContent())) {
                        ApiDefinitionExecResultWithBLOBs execResult = resultMap.get(item.getReportId());
                        Map responseObj = new LinkedHashMap();
                        try {
                            responseObj = JSON.parseMap(execResult.getContent());
                        } catch (Exception e) {
                        }
                        if (StringUtils.isNotEmpty(execResult.getEnvConfig())) {
                            responseObj.put("envName", apiDefinitionService.getEnvNameByEnvConfig(execResult.getProjectId(), execResult.getEnvConfig()));
                        }
                        item.setResponse(responseObj.toString());
                    }
                });
            }
        }
    }

    private void screenApiCaseByStatusAndReportConfig(ApiPlanReportDTO report, List<TestPlanFailureApiDTO> apiAllCases, Map reportConfig) {
        if (!CollectionUtils.isEmpty(apiAllCases)) {
            List<TestPlanFailureApiDTO> apiFailureCases = new ArrayList<>();
            List<TestPlanFailureApiDTO> apiErrorReportCases = new ArrayList<>();
            List<TestPlanFailureApiDTO> apiUnExecuteCases = new ArrayList<>();
            for (TestPlanFailureApiDTO apiDTO : apiAllCases) {
                if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.ERROR.name())) {
                    apiFailureCases.add(apiDTO);
                } else if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.FAKE_ERROR.name())) {
                    apiErrorReportCases.add(apiDTO);
                } else if (StringUtils.equalsAnyIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.STOPPED.name(),
                        ApiReportStatus.PENDING.name())) {
                    apiUnExecuteCases.add(apiDTO);
                }
            }

            if (checkReportConfig(reportConfig, "api", "failure")) {
                report.setApiFailureCases(apiFailureCases);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.FAKE_ERROR.name())) {
                report.setErrorReportCases(apiErrorReportCases);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.PENDING.name())) {
                report.setUnExecuteCases(apiUnExecuteCases);
            }
        }
    }

    private boolean checkReportConfig(Map config, String key, String subKey) {
        return ServiceUtils.checkConfigEnable(config, key, subKey);
    }

    private void screenScenariosByStatusAndReportConfig(ApiPlanReportDTO report, List<TestPlanFailureScenarioDTO> scenarios, Map reportConfig) {

        if (!CollectionUtils.isEmpty(scenarios)) {
            List<TestPlanFailureScenarioDTO> failureScenarios = new ArrayList<>();
            List<TestPlanFailureScenarioDTO> errorReportScenarios = new ArrayList<>();
            List<TestPlanFailureScenarioDTO> unExecuteScenarios = new ArrayList<>();
            for (TestPlanFailureScenarioDTO scenario : scenarios) {
                if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), ApiReportStatus.ERROR.name())) {
                    failureScenarios.add(scenario);
                } else if (StringUtils.equalsIgnoreCase(scenario.getLastResult(), ApiReportStatus.FAKE_ERROR.name())) {
                    errorReportScenarios.add(scenario);
                } else if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), ApiReportStatus.STOPPED.name(),
                        ApiReportStatus.PENDING.name())) {
                    unExecuteScenarios.add(scenario);
                }
            }
            if (checkReportConfig(reportConfig, "api", "failure")) {
                report.setScenarioFailureCases(failureScenarios);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.FAKE_ERROR.name())) {
                report.setErrorReportScenarios(errorReportScenarios);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.PENDING.name())) {
                report.setUnExecuteScenarios(unExecuteScenarios);
            }
        }
    }

    public ApiPlanReportDTO buildExecuteApiReport(ApiPlanReportRequest request) {
        ApiPlanReportDTO report = new ApiPlanReportDTO();
        TestPlanExecuteReportDTO testPlanExecuteReportDTO = request.getTestPlanExecuteReportDTO();
        Map config = request.getConfig();
        if (MapUtils.isEmpty(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap())
                && MapUtils.isEmpty(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap())) {
            return new ApiPlanReportDTO();
        }
        if (ServiceUtils.checkConfigEnable(config, "api")) {
            List<TestPlanFailureApiDTO> apiAllCases = null;
            List<TestPlanFailureScenarioDTO> scenarioAllCases = null;
            if (checkReportConfig(config, "api", "all")) {
                // 接口
                apiAllCases = getByApiExecReportIds(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap(), testPlanExecuteReportDTO.getApiCaseInfoDTOMap());
                //场景
                scenarioAllCases = getTestPlanScenario(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap(), testPlanExecuteReportDTO.getScenarioInfoDTOMap());
                this.checkApiCaseCreatorName(apiAllCases, scenarioAllCases);
                report.setApiAllCases(apiAllCases);
                report.setScenarioAllCases(scenarioAllCases);
            }

            //筛选符合配置需要的执行结果的用例和场景
            this.screenApiCaseByStatusAndReportConfig(report, apiAllCases, config);
            this.screenScenariosByStatusAndReportConfig(report, scenarioAllCases, config);
        }
        return report;
    }


    private void checkApiCaseCreatorName(List<TestPlanFailureApiDTO> apiCases, List<TestPlanFailureScenarioDTO> scenarioCases) {
        List<String> userIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(apiCases)) {
            apiCases.forEach(item -> {
                if (StringUtils.isEmpty(item.getCreatorName()) && StringUtils.isNotEmpty(item.getCreateUserId())) {
                    userIdList.add(item.getCreateUserId());
                }
            });
        }
        if (CollectionUtils.isNotEmpty(scenarioCases)) {
            scenarioCases.forEach(item -> {
                if (StringUtils.isEmpty(item.getCreatorName()) && StringUtils.isNotEmpty(item.getCreateUser())) {
                    userIdList.add(item.getCreateUser());
                }
            });
        }
        Map<String, User> usersMap = baseUserService.queryNameByIds(userIdList);
        if (CollectionUtils.isNotEmpty(apiCases)) {
            for (TestPlanFailureApiDTO dto : apiCases) {
                if (StringUtils.isEmpty(dto.getCreatorName())) {
                    User user = usersMap.get(dto.getCreateUserId());
                    if (user != null) {
                        dto.setCreatorName(user.getName());
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(scenarioCases)) {
            for (TestPlanFailureScenarioDTO dto : scenarioCases) {
                if (StringUtils.isEmpty(dto.getCreatorName())) {
                    User user = usersMap.get(dto.getCreateUser());
                    if (user != null) {
                        dto.setCreatorName(user.getName());
                    }
                }
            }
        }

    }

    public List<TestPlanFailureScenarioDTO> getTestPlanScenario(Map<String, String> idMap, Map<String, TestPlanFailureScenarioDTO> scenarioInfoDTOMap) {
        Map<String, String> reportStatus = apiScenarioReportService.getReportStatusByReportIds(idMap.values());
        Map<String, String> savedReportMap = new HashMap<>(idMap);
        List<TestPlanFailureScenarioDTO> apiTestCases = new ArrayList<>();
        for (TestPlanFailureScenarioDTO dto : scenarioInfoDTOMap.values()) {
            String reportId = savedReportMap.get(dto.getId());
            savedReportMap.remove(dto.getId());
            dto.setReportId(reportId);
            if (StringUtils.isNotEmpty(reportId)) {
                String status = reportStatus.get(reportId);
                if (status == null) {
                    status = ApiReportStatus.ERROR.name();
                }
                dto.setLastResult(status);
                dto.setStatus(status);
            }
            apiTestCases.add(dto);
        }
        return buildCases(apiTestCases);
    }


    public List<TestPlanFailureApiDTO> getByApiExecReportIds(Map<String, String> testPlanApiCaseReportMap, Map<String, TestPlanFailureApiDTO> apiCaseInfoDTOMap) {
        if (testPlanApiCaseReportMap.isEmpty()) {
            return new ArrayList<>();
        }
        String defaultStatus = ApiReportStatus.ERROR.name();
        Map<String, String> reportResult = apiDefinitionExecResultService.selectReportResultByReportIds(testPlanApiCaseReportMap.values());
        Map<String, String> savedReportMap = new HashMap<>(testPlanApiCaseReportMap);
        List<TestPlanFailureApiDTO> apiTestCases = new ArrayList<>();
        for (TestPlanFailureApiDTO dto : apiCaseInfoDTOMap.values()) {
            String testPlanApiCaseId = dto.getId();
            String reportId = savedReportMap.get(testPlanApiCaseId);
            savedReportMap.remove(testPlanApiCaseId);
            dto.setReportId(reportId);
            if (StringUtils.isEmpty(reportId)) {
                dto.setExecResult(defaultStatus);
            } else {
                String status = reportResult.get(reportId);
                if (status == null) {
                    status = defaultStatus;
                }
                dto.setExecResult(status);
            }
            apiTestCases.add(dto);
        }
        return testPlanApiCaseService.buildCases(apiTestCases);
    }

    public TestPlanApiReportInfoDTO genApiReportInfoForSchedule(String planId, RunModeConfigDTO runModeConfigDTO) {
        TestPlanApiReportInfoDTO testPlanApiReportInfo = new TestPlanApiReportInfoDTO();
        Map<String, String> planApiCaseIdMap = new LinkedHashMap<>();
        Map<String, String> planScenarioIdMap = new LinkedHashMap<>();

        List<TestPlanApiScenarioInfoDTO> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectLegalDataByTestPlanId(planId);
        for (TestPlanApiScenarioInfoDTO model : testPlanApiScenarioList) {
            planScenarioIdMap.put(model.getId(), model.getApiScenarioId());
        }
        List<TestPlanApiCaseInfoDTO> testPlanApiCaseList = extTestPlanApiCaseMapper.selectLegalDataByTestPlanId(planId);
        for (TestPlanApiCaseInfoDTO model : testPlanApiCaseList) {
            planApiCaseIdMap.put(model.getId(), model.getApiCaseId());
        }

        testPlanApiReportInfo.setPlanApiCaseIdMap(planApiCaseIdMap);
        testPlanApiReportInfo.setPlanScenarioIdMap(planScenarioIdMap);
        //解析运行环境信息
        TestPlanReportRunInfoDTO runInfoDTO = this.parseTestPlanRunInfo(runModeConfigDTO, testPlanApiCaseList, testPlanApiScenarioList);
        testPlanApiReportInfo.setRunInfoDTO(runInfoDTO);
        return testPlanApiReportInfo;
    }

    public TestPlanReportRunInfoDTO parseTestPlanRunInfo(RunModeConfigDTO runModeConfigDTO, List<TestPlanApiCaseInfoDTO> testPlanApiCaseList, List<TestPlanApiScenarioInfoDTO> testPlanApiScenarioList) {
        TestPlanReportRunInfoDTO runInfoDTO = new TestPlanReportRunInfoDTO();
        Map<String, String> selectEnvMap = runModeConfigDTO.getEnvMap();
        runInfoDTO.setRunMode(runModeConfigDTO.getMode());
        if (StringUtils.equals("GROUP", runModeConfigDTO.getEnvironmentType()) && StringUtils.isNotEmpty(runModeConfigDTO.getEnvironmentGroupId())) {
            selectEnvMap = environmentGroupProjectService.getEnvMap(runModeConfigDTO.getEnvironmentGroupId());
            runInfoDTO.setEnvGroupId(runModeConfigDTO.getEnvironmentGroupId());
        }

        for (TestPlanApiScenarioInfoDTO model : testPlanApiScenarioList) {
            Map<String, String> envMap = null;
            if (StringUtils.equalsIgnoreCase("group", model.getEnvironmentType()) && StringUtils.isNotEmpty(model.getEnvironmentGroupId())) {
                envMap = environmentGroupProjectService.getEnvMap(model.getEnvironmentGroupId());
            } else {
                if (MapUtils.isNotEmpty(selectEnvMap) && selectEnvMap.containsKey(model.getProjectId())) {
                    runInfoDTO.putScenarioRunInfo(model.getId(), model.getProjectId(), selectEnvMap.get(model.getProjectId()));
                } else if (StringUtils.isNotEmpty(model.getEnvironment())) {
                    try {
                        envMap = JSON.parseObject(model.getEnvironment(), Map.class);
                    } catch (Exception e) {
                        LogUtil.error("解析场景环境失败!", e);
                    }
                }
            }
            if (MapUtils.isNotEmpty(envMap)) {
                for (Map.Entry<String, String> entry : envMap.entrySet()) {
                    String projectId = entry.getKey();
                    String envIdStr = entry.getValue();
                    runInfoDTO.putScenarioRunInfo(model.getId(), projectId, envIdStr);
                }
            }
        }
        for (TestPlanApiCaseInfoDTO model : testPlanApiCaseList) {
            if (MapUtils.isNotEmpty(selectEnvMap) && selectEnvMap.containsKey(model.getProjectId())) {
                runInfoDTO.putApiCaseRunInfo(model.getId(), model.getProjectId(), selectEnvMap.get(model.getProjectId()));
            } else {
                runInfoDTO.putApiCaseRunInfo(model.getId(), model.getProjectId(), model.getEnvironmentId());
            }
        }
        return runInfoDTO;
    }

    public Boolean isExecuting(String planId) {
        List<TestPlanApiScenarioInfoDTO> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectLegalDataByTestPlanId(planId);
        return !testPlanApiScenarioList.stream()
                .map(TestPlanApiScenarioInfoDTO::getApiScenarioId)
                .collect(Collectors.toList())
                .isEmpty();
    }

    public List<TestPlanFailureScenarioDTO> getFailureListByIds(Set<String> ids) {
        return extTestPlanScenarioCaseMapper.getFailureListByIds(ids, null);
    }

    public TestPlanEnvInfoDTO generateEnvironmentInfo(TestPlanReport testPlanReport) {
        TestPlanEnvInfoDTO testPlanEnvInfoDTO = new TestPlanEnvInfoDTO();
        TestPlanReportRunInfoDTO runInfoDTO = null;
        if (StringUtils.isNotEmpty(testPlanReport.getRunInfo())) {
            try {
                runInfoDTO = JSON.parseObject(testPlanReport.getRunInfo(), TestPlanReportRunInfoDTO.class);
            } catch (Exception e) {
                LogUtil.error("解析测试计划报告记录的运行环境信息[" + testPlanReport.getRunInfo() + "]时出错!", e);
            }

        }
        if (runInfoDTO != null) {
            if (StringUtils.isNotEmpty(runInfoDTO.getEnvGroupId())) {
                EnvironmentGroup environmentGroup = apiTestEnvironmentService.selectById(runInfoDTO.getEnvGroupId());
                if (StringUtils.isNotEmpty(environmentGroup.getName())) {
                    testPlanEnvInfoDTO.setEnvGroupName(environmentGroup.getName());
                }
            }
            testPlanEnvInfoDTO.setRunMode(StringUtils.equalsIgnoreCase(runInfoDTO.getRunMode(), "serial") ? Translator.get("serial") : Translator.get("parallel"));
            Map<String, Set<String>> projectEnvMap = new LinkedHashMap<>();
            if (MapUtils.isNotEmpty(runInfoDTO.getApiCaseRunInfo())) {
                this.setApiCaseProjectEnvMap(projectEnvMap, runInfoDTO.getApiCaseRunInfo());
            }
            if (MapUtils.isNotEmpty(runInfoDTO.getScenarioRunInfo())) {
                this.setScenarioProjectEnvMap(projectEnvMap, runInfoDTO.getScenarioRunInfo());
            }
            Map<String, List<String>> showProjectEnvMap = new LinkedHashMap<>();
            for (Map.Entry<String, Set<String>> entry : projectEnvMap.entrySet()) {
                String projectId = entry.getKey();
                Set<String> envIdSet = entry.getValue();
                Project project = baseProjectService.getProjectById(projectId);
                String projectName = project == null ? null : project.getName();
                List<String> envNames = apiTestEnvironmentService.selectNameByIds(envIdSet);
                if (StringUtils.isNotEmpty(projectName) && CollectionUtils.isNotEmpty(envNames)) {
                    showProjectEnvMap.put(projectName, envNames);
                }
            }
            if (MapUtils.isNotEmpty(showProjectEnvMap)) {
                testPlanEnvInfoDTO.setProjectEnvMap(showProjectEnvMap);
            }
        }
        return testPlanEnvInfoDTO;
    }

    private void setApiCaseProjectEnvMap(Map<String, Set<String>> projectEnvMap, Map<String, Map<String, String>> caseEnvironmentMap) {
        if (projectEnvMap == null || caseEnvironmentMap == null) {
            return;
        }
        for (Map<String, String> map : caseEnvironmentMap.values()) {
            if (MapUtils.isEmpty(map)) {
                continue;
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String projectId = entry.getKey();
                String envId = entry.getValue();
                if (projectEnvMap.containsKey(projectId)) {
                    projectEnvMap.get(projectId).add(envId);
                } else {
                    projectEnvMap.put(projectId, new LinkedHashSet<>() {{
                        this.add(envId);
                    }});
                }
            }
        }
    }

    private void setScenarioProjectEnvMap(Map<String, Set<String>> projectEnvMap, Map<String, Map<String, List<String>>> caseEnvironmentMap) {
        if (projectEnvMap == null || caseEnvironmentMap == null) {
            return;
        }
        for (Map<String, List<String>> map : caseEnvironmentMap.values()) {
            if (MapUtils.isEmpty(map)) {
                continue;
            }
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String projectId = entry.getKey();
                List<String> envIdList = entry.getValue();
                if (CollectionUtils.isNotEmpty(envIdList)) {
                    envIdList.forEach(envId -> {
                        if (projectEnvMap.containsKey(projectId)) {
                            projectEnvMap.get(projectId).add(envId);
                        } else {
                            projectEnvMap.put(projectId, new LinkedHashSet<>() {{
                                this.add(envId);
                            }});
                        }
                    });
                }
            }
        }
    }

    public List<PlanReportCaseDTO> selectStatusForPlanReport(String planId) {
        return extTestPlanScenarioCaseMapper.selectForPlanReport(planId);
    }


    public List<ApiScenarioModuleDTO> getNodeByPlanId(List<String> projectIds, String planId) {
        List<ApiScenarioModuleDTO> list = new ArrayList<>();
        projectIds.forEach(id -> {
            Project project = baseProjectService.getProjectById(id);
            String name = project.getName();
            List<ApiScenarioModuleDTO> nodeList = getNodeDTO(id, planId);
            ApiScenarioModuleDTO scenarioModuleDTO = new ApiScenarioModuleDTO();
            scenarioModuleDTO.setId(project.getId());
            scenarioModuleDTO.setName(name);
            scenarioModuleDTO.setLabel(name);
            scenarioModuleDTO.setChildren(nodeList);
            if (!org.springframework.util.CollectionUtils.isEmpty(nodeList)) {
                list.add(scenarioModuleDTO);
            }
        });
        return list;
    }

    private List<ApiScenarioModuleDTO> getNodeDTO(String projectId, String planId) {
        List<TestPlanApiScenario> apiCases = getCasesByPlanId(planId);
        if (apiCases.isEmpty()) {
            return null;
        }
        List<ApiScenarioModuleDTO> testCaseNodes = extApiScenarioModuleMapper.getNodeTreeByProjectId(projectId);

        List<String> caseIds = apiCases.stream()
                .map(TestPlanApiScenario::getApiScenarioId)
                .collect(Collectors.toList());

        List<String> dataNodeIds = apiAutomationService.selectByIds(caseIds).stream()
                .filter(apiScenario -> apiScenario.getStatus() == null || !CommonConstants.TrashStatus.equals(apiScenario.getStatus()))
                .map(ApiScenario::getApiScenarioModuleId)
                .collect(Collectors.toList());

        List<ApiScenarioModuleDTO> nodeTrees = apiScenarioModuleService.getNodeTrees(testCaseNodes);

        Iterator<ApiScenarioModuleDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            ApiScenarioModuleDTO rootNode = iterator.next();
            if (apiScenarioModuleService.pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }
        return nodeTrees;
    }
}
