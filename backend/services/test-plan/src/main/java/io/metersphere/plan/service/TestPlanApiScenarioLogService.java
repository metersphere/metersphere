package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseExample;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.domain.TestPlanApiCaseExample;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.domain.TestPlanApiScenarioExample;
import io.metersphere.plan.dto.request.BaseBatchMoveRequest;
import io.metersphere.plan.dto.request.TestPlanApiCaseUpdateRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioUpdateRequest;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanApiScenarioMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiScenarioLogService {

    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    public void batchUpdateExecutor(TestPlanApiScenarioUpdateRequest request) {
        List<String> ids = testPlanApiScenarioService.doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andIdIn(ids);
            List<TestPlanApiScenario> planApiScenarioList = testPlanApiScenarioMapper.selectByExample(example);
            Map<String, String> userMap = planApiScenarioList.stream().collect(Collectors.toMap(TestPlanApiScenario::getId, TestPlanApiScenario::getExecuteUser));
            Map<String, String> idsMap = planApiScenarioList.stream().collect(Collectors.toMap(TestPlanApiScenario::getId, TestPlanApiScenario::getApiScenarioId));
            List<String> scenarioIds = planApiScenarioList.stream().map(TestPlanApiScenario::getApiScenarioId).collect(Collectors.toList());
            ApiScenarioExample scenarioExample = new ApiScenarioExample();
            scenarioExample.createCriteria().andIdIn(scenarioIds);
            List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(scenarioExample);
            Map<String, String> caseMap = apiScenarios.stream().collect(Collectors.toMap(ApiScenario::getId, ApiScenario::getName));
            List<LogDTO> dtoList = new ArrayList<>();
            idsMap.forEach((k, v) -> {
                LogDTO dto = new LogDTO(
                        null,
                        null,
                        k,
                        null,
                        OperationLogType.UPDATE.name(),
                        OperationLogModule.TEST_PLAN,
                        caseMap.get(v));
                dto.setPath("/test-plan/api/scenario/batch/update/executor");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(userMap.get(k)));
                dto.setModifiedValue(JSON.toJSONBytes(request.getUserId()));
                dtoList.add(dto);
            });
        }
    }

    public void batchMove(BaseBatchMoveRequest request) {
        List<String> ids = testPlanApiScenarioService.doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andIdIn(ids);
            List<TestPlanApiScenario> caseList = testPlanApiScenarioMapper.selectByExample(example);
            List<String> apiScenarioIds = caseList.stream().map(TestPlanApiScenario::getApiScenarioId).collect(Collectors.toList());
            ApiScenarioExample scenarioExample = new ApiScenarioExample();
            scenarioExample.createCriteria().andIdIn(apiScenarioIds);
            List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(scenarioExample);
            Map<String, String> caseMap = apiScenarios.stream().collect(Collectors.toMap(ApiScenario::getId, ApiScenario::getName));
            List<LogDTO> dtoList = new ArrayList<>();
            caseList.forEach(item -> {
                LogDTO dto = new LogDTO(
                        null,
                        null,
                        item.getApiScenarioId(),
                        null,
                        OperationLogType.UPDATE.name(),
                        OperationLogModule.TEST_PLAN,
                        Translator.get("move") + ":" + caseMap.get(item.getApiScenarioId()));
                dto.setPath("/test-plan/api/scenario/batch/move");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(item));
                TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
                testPlanApiScenario.setId(item.getId());
                testPlanApiScenario.setTestPlanCollectionId(request.getTargetCollectionId());
                dto.setModifiedValue(JSON.toJSONBytes(testPlanApiScenario));
                dtoList.add(dto);
            });

        }
    }

}



