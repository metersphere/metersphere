package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.domain.TestPlanApiScenarioExample;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.track.dto.RelevanceScenarioRequest;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
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

    public List<ApiScenarioDTO> list(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiScenarioDTO> apiTestCases = extTestPlanScenarioCaseMapper.list(request);
        setApiScenarioProjectIds(apiTestCases);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        return apiTestCases;
    }

    private void setApiScenarioProjectIds(List<ApiScenarioDTO> list) {
        // 如果场景步骤涉及多项目，则把涉及到的项目ID保存在projectIds属性
        list.forEach(data -> {
            List<String> idList = new ArrayList<>();
            String definition = data.getScenarioDefinition();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(definition)) {
                RunDefinitionRequest d = JSON.parseObject(definition, RunDefinitionRequest.class);

                if (d != null) {
                    Map<String, String> map = d.getEnvironmentMap();
                    if (map != null) {
                        if (map.isEmpty()) {
                            List<String> ids = (List<String>) JSONPath.read(definition, "$..projectId");
                            idList.addAll(new HashSet<>(ids));
                        } else {
                            Set<String> set = d.getEnvironmentMap().keySet();
                            idList = new ArrayList<>(set);
                        }
                    } else {
                        // 兼容历史数据，无EnvironmentMap直接赋值场景所属项目
                        idList.add(data.getProjectId());
                    }
                }

            }
            data.setProjectIds(idList);
        });
    }

    public List<ApiScenarioDTO> relevanceList(ApiScenarioRequest request) {
        request.setNotInTestPlan(true);
        List<ApiScenarioDTO> list = apiAutomationService.list(request);
        setApiScenarioProjectIds(list);
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

    public void deleteApiCaseBath(TestPlanApiCaseBatchRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria()
                .andIdIn(request.getIds());
        List<String> reportIds = testPlanApiScenarioMapper.selectByExample(example).stream()
                .map(TestPlanApiScenario::getReportId).collect(Collectors.toList());
        apiScenarioReportService.deleteByIds(reportIds);
        testPlanApiScenarioMapper.deleteByExample(example);
    }

    public String run(RunScenarioRequest request) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andIdIn(request.getPlanCaseIds());
        List<TestPlanApiScenario> testPlanApiScenarioList = testPlanApiScenarioMapper.selectByExample(example);

        List<String> scenarioIds = new ArrayList<>();
        Map<String,String> scenarioIdApiScarionMap = new HashMap<>();
        for (TestPlanApiScenario apiScenario:
                testPlanApiScenarioList) {
            scenarioIds.add(apiScenario.getApiScenarioId());
            scenarioIdApiScarionMap.put(apiScenario.getApiScenarioId(),apiScenario.getId());
        }
        request.setIds(scenarioIds);
        request.setScenarioTestPlanIdMap(scenarioIdApiScarionMap);
        request.setRunMode(ApiRunMode.SCENARIO_PLAN.name());
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
        TestPlanApiCaseBatchRequest request = new TestPlanApiCaseBatchRequest();
        List<String> ids = extTestPlanScenarioCaseMapper.getIdsByPlanId(planId);
        request.setIds(ids);
        deleteApiCaseBath(request);
    }

    public void deleteByRelevanceProjectIds(String planId, List<String> relevanceProjectIds) {
        TestPlanApiCaseBatchRequest request = new TestPlanApiCaseBatchRequest();
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
        if (set.isEmpty()) { return; }
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
}
