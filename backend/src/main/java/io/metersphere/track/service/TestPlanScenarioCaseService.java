package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.service.ProjectService;
import io.metersphere.track.dto.RelevanceScenarioRequest;
import io.metersphere.track.request.testcase.TestPlanScenarioCaseBatchRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    public List<ApiScenarioDTO> list(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiScenarioDTO> apiTestCases = extTestPlanScenarioCaseMapper.list(request);
        List<String> projectIds = apiTestCases.stream()
                .map(ApiScenarioDTO::getProjectId)
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toList());

        Map<String, Project> projectMap = projectService.getProjectByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, project -> project));

        apiTestCases.forEach(item -> {
            Project project = projectMap.get(item.getProjectId());
            if (project.getScenarioCustomNum() != null && project.getScenarioCustomNum()) {
                item.setCustomNum(item.getCustomNum());
            } else {
                item.setCustomNum(item.getNum().toString());
            }
        });

        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildUserInfo(apiTestCases);
        return apiTestCases;
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
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<String> idList = extTestPlanScenarioCaseMapper.selectIds(request);
        return idList;
    }

    public List<ApiScenarioDTO> relevanceList(ApiScenarioRequest request) {
        request.setNotInTestPlan(true);
        List<ApiScenarioDTO> list = apiAutomationService.list(request);
        return list;
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

    public String run(RunTestPlanScenarioRequest testPlanScenarioRequest) {
        StringBuilder idStr = new StringBuilder();
        List<String> planCaseIdList = testPlanScenarioRequest.getPlanCaseIds();
        if (testPlanScenarioRequest.getCondition() != null && testPlanScenarioRequest.getCondition().isSelectAll()) {
            planCaseIdList = this.selectIds(testPlanScenarioRequest.getCondition());
            if (testPlanScenarioRequest.getCondition().getUnSelectIds() != null) {
                planCaseIdList.removeAll(testPlanScenarioRequest.getCondition().getUnSelectIds());
            }
        }
        testPlanScenarioRequest.setPlanCaseIds(planCaseIdList);
        planCaseIdList.forEach(item -> {
            idStr.append("\"").append(item).append("\"").append(",");
        });
        List<TestPlanApiScenario> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectByIds(idStr.toString().substring(0, idStr.toString().length() - 1), "\"" + org.apache.commons.lang3.StringUtils.join(testPlanScenarioRequest.getPlanCaseIds(), ",") + "\"");
        List<String> scenarioIds = new ArrayList<>();
        Map<String, String> scenarioIdApiScarionMap = new HashMap<>();
        for (TestPlanApiScenario apiScenario : testPlanApiScenarioList) {
            scenarioIds.add(apiScenario.getApiScenarioId());
            scenarioIdApiScarionMap.put(apiScenario.getApiScenarioId(), apiScenario.getId());
        }

        RunScenarioRequest request = new RunScenarioRequest();
        request.setIds(scenarioIds);
        request.setReportId(testPlanScenarioRequest.getId());
        request.setScenarioTestPlanIdMap(scenarioIdApiScarionMap);
        request.setRunMode(ApiRunMode.SCENARIO_PLAN.name());
        request.setId(testPlanScenarioRequest.getId());
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(testPlanScenarioRequest.getTriggerMode());
        request.setConfig(testPlanScenarioRequest.getConfig());
        return apiAutomationService.run(request);
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
        Map<String, List<String>> mapping = request.getMapping();
        Set<String> set = mapping.keySet();
        if (set.isEmpty()) {
            return;
        }
        request.setIds(new ArrayList<>(set));
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
                scenario.setEnvironment(JSON.toJSONString(newEnvMap));
                testPlanApiScenarioMapper.updateByPrimaryKeySelective(scenario);
            }
        });


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
}
