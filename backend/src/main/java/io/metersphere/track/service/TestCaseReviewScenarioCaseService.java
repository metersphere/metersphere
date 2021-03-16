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
import io.metersphere.base.domain.TestCaseReviewScenario;
import io.metersphere.base.domain.TestCaseReviewScenarioExample;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.domain.TestPlanApiScenarioExample;
import io.metersphere.base.mapper.TestCaseReviewScenarioMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseReviewScenarioCaseMapper;
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
public class TestCaseReviewScenarioCaseService {
    @Resource
    ApiAutomationService apiAutomationService;
    @Resource
    TestCaseReviewScenarioMapper testCaseReviewScenarioMapper;
    @Resource
    ExtTestCaseReviewScenarioCaseMapper extTestCaseReviewScenarioCaseMapper;
    @Resource
    ApiScenarioReportService apiScenarioReportService;
    public List<ApiScenarioDTO> list(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiScenarioDTO> apiTestCases = extTestCaseReviewScenarioCaseMapper.list(request);
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
        List<ApiScenarioDTO> list = apiAutomationService.listReview(request);
        setApiScenarioProjectIds(list);
        return list;
    }

    public int delete(String id) {
        TestCaseReviewScenario testCaseReviewScenario = testCaseReviewScenarioMapper.selectByPrimaryKey(id);
        String reportId = testCaseReviewScenario.getReportId();
        if (!StringUtils.isEmpty(reportId)) {
            apiScenarioReportService.delete(reportId);
        }
       TestCaseReviewScenarioExample example = new TestCaseReviewScenarioExample();
        example.createCriteria()
                .andIdEqualTo(id);

        return testCaseReviewScenarioMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanApiCaseBatchRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        TestCaseReviewScenarioExample example = new TestCaseReviewScenarioExample();
        example.createCriteria()
                .andIdIn(request.getIds());
        List<String> reportIds = testCaseReviewScenarioMapper.selectByExample(example).stream()
                .map(TestCaseReviewScenario::getReportId).collect(Collectors.toList());
        apiScenarioReportService.deleteByIds(reportIds);
        testCaseReviewScenarioMapper.deleteByExample(example);
    }

    public String run(RunScenarioRequest request) {
        TestCaseReviewScenarioExample example = new TestCaseReviewScenarioExample();
        example.createCriteria().andIdIn(request.getPlanCaseIds());
        List<TestCaseReviewScenario> testPlanApiScenarioList = testCaseReviewScenarioMapper.selectByExample(example);

        List<String> scenarioIds = new ArrayList<>();
        Map<String,String> scenarioIdApiScarionMap = new HashMap<>();
        for (TestCaseReviewScenario apiScenario:
                testPlanApiScenarioList) {
            scenarioIds.add(apiScenario.getApiScenarioId());
            scenarioIdApiScarionMap.put(apiScenario.getApiScenarioId(),apiScenario.getId());
        }
        request.setIds(scenarioIds);
        request.setScenarioTestPlanIdMap(scenarioIdApiScarionMap);
        request.setRunMode(ApiRunMode.SCENARIO_PLAN.name());
        return apiAutomationService.run(request);
    }

    public List<TestCaseReviewScenario> getCasesByReviewId(String reviewId) {
        TestCaseReviewScenarioExample example = new TestCaseReviewScenarioExample();
        example.createCriteria().andTestCaseReviewIdEqualTo(reviewId);
        return testCaseReviewScenarioMapper.selectByExample(example);
    }

    public List<String> getExecResultByReviewId(String reviewId) {
        return extTestCaseReviewScenarioCaseMapper.getExecResultByReviewId(reviewId);
    }

    public void deleteByReviewId(String reviewId) {
        TestPlanApiCaseBatchRequest request = new TestPlanApiCaseBatchRequest();
        List<String> ids = extTestCaseReviewScenarioCaseMapper.getIdsByReviewId(reviewId);
        request.setIds(ids);
        deleteApiCaseBath(request);
    }

    public void deleteByRelevanceProjectIds(String reviewId, List<String> relevanceProjectIds) {
        TestPlanApiCaseBatchRequest request = new TestPlanApiCaseBatchRequest();
        request.setIds(extTestCaseReviewScenarioCaseMapper.getNotRelevanceCaseIds(reviewId, relevanceProjectIds));
        request.setPlanId(reviewId);
        deleteApiCaseBath(request);
    }

    public void bathDeleteByScenarioIds(List<String> ids) {
        TestCaseReviewScenarioExample example = new TestCaseReviewScenarioExample();
        example.createCriteria().andApiScenarioIdIn(ids);
        testCaseReviewScenarioMapper.deleteByExample(example);
    }

    public void deleteByScenarioId(String id) {
        TestCaseReviewScenarioExample example = new TestCaseReviewScenarioExample();
        example.createCriteria().andApiScenarioIdEqualTo(id);
        testCaseReviewScenarioMapper.deleteByExample(example);
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
                TestCaseReviewScenario scenario = new TestCaseReviewScenario();
                scenario.setId(id);
                scenario.setEnvironment(JSON.toJSONString(newEnvMap));
                testCaseReviewScenarioMapper.updateByPrimaryKeySelective(scenario);
            }
        });


    }
}
