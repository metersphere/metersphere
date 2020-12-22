package io.metersphere.track.service;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.domain.TestPlanApiScenarioExample;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public List<ApiScenarioDTO> list(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiScenarioDTO> apiTestCases = extTestPlanScenarioCaseMapper.list(request);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        return apiTestCases;
    }

    public List<ApiScenarioDTO> relevanceList(ApiScenarioRequest request) {
        List<String> ids = apiAutomationService.selectIdsNotExistsInPlan(request.getProjectId(), request.getPlanId());
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        request.setIds(ids);
        return apiAutomationService.list(request);
    }

    public int delete(String planId, String id) {
//        apiDefinitionExecResultService.deleteByResourceId(id);
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andIdEqualTo(id);

        return testPlanApiScenarioMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanApiCaseBatchRequest request) {
//        apiDefinitionExecResultService.deleteByResourceIds(request.getIds());
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria()
                .andIdIn(request.getIds())
                .andTestPlanIdEqualTo(request.getPlanId());
        testPlanApiScenarioMapper.deleteByExample(example);
    }

    public String run(RunScenarioRequest request) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andIdIn(request.getPlanCaseIds());
        List<String> scenarioIds = testPlanApiScenarioMapper.selectByExample(example).stream()
                .map(TestPlanApiScenario::getApiScenarioId)
                .collect(Collectors.toList());
        scenarioIds.addAll(scenarioIds);
        request.setScenarioIds(scenarioIds);
        request.setRunMode(ApiRunMode.SCENARIO_PLAN.name());
        return apiAutomationService.run(request);
    }

    public List<TestPlanApiScenario> getCasesByPlanId(String planId) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanApiScenarioMapper.selectByExample(example);
    }
}
