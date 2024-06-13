package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseExample;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.domain.TestPlanApiCaseExample;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.dto.request.TestPlanApiCaseBatchMoveRequest;
import io.metersphere.plan.dto.request.TestPlanApiCaseUpdateRequest;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
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
public class TestPlanApiCaseLogService {

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;

    public void batchUpdateExecutor(TestPlanApiCaseUpdateRequest request) {
        List<String> ids = testPlanApiCaseService.doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            TestPlanApiCaseExample example = new TestPlanApiCaseExample();
            example.createCriteria().andIdIn(ids);
            List<TestPlanApiCase> planCaseList = testPlanApiCaseMapper.selectByExample(example);
            Map<String, String> userMap = planCaseList.stream().collect(Collectors.toMap(TestPlanApiCase::getId, TestPlanApiCase::getExecuteUser));
            Map<String, String> idsMap = planCaseList.stream().collect(Collectors.toMap(TestPlanApiCase::getId, TestPlanApiCase::getApiCaseId));
            List<String> caseIds = planCaseList.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
            ApiTestCaseExample caseExample = new ApiTestCaseExample();
            caseExample.createCriteria().andIdIn(caseIds);
            List<ApiTestCase> functionalCases = apiTestCaseMapper.selectByExample(caseExample);
            Map<String, String> caseMap = functionalCases.stream().collect(Collectors.toMap(ApiTestCase::getId, ApiTestCase::getName));
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
                dto.setPath("/test-plan/api/case/batch/update/executor");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(userMap.get(k)));
                dto.setModifiedValue(JSON.toJSONBytes(request.getUserId()));
                dtoList.add(dto);
            });
        }
    }

    public void batchMove(TestPlanApiCaseBatchMoveRequest request) {
        List<String> ids = testPlanApiCaseService.doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            TestPlanApiCaseExample example = new TestPlanApiCaseExample();
            example.createCriteria().andIdIn(ids);
            List<TestPlanApiCase> caseList = testPlanApiCaseMapper.selectByExample(example);
            List<String> apiCaseIds = caseList.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
            ApiTestCaseExample caseExample = new ApiTestCaseExample();
            caseExample.createCriteria().andIdIn(apiCaseIds);
            List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(caseExample);
            Map<String, String> caseMap = apiTestCases.stream().collect(Collectors.toMap(ApiTestCase::getId, ApiTestCase::getName));
            List<LogDTO> dtoList = new ArrayList<>();
            caseList.forEach(item -> {
                LogDTO dto = new LogDTO(
                        null,
                        null,
                        item.getApiCaseId(),
                        null,
                        OperationLogType.UPDATE.name(),
                        OperationLogModule.TEST_PLAN,
                        Translator.get("move") + ":" + caseMap.get(item.getApiCaseId()));
                dto.setPath("/test-plan/api/case/batch/move");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(item));
                TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
                testPlanApiCase.setId(item.getId());
                testPlanApiCase.setTestPlanCollectionId(request.getTargetCollectionId());
                dto.setModifiedValue(JSON.toJSONBytes(testPlanApiCase));
                dtoList.add(dto);
            });
        }
    }
}



