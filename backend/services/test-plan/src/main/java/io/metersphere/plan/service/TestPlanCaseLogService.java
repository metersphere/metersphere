package io.metersphere.plan.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseExample;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.dto.request.BaseBatchMoveRequest;
import io.metersphere.plan.dto.request.TestPlanCaseUpdateRequest;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
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
public class TestPlanCaseLogService {

    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionalCaseService;
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TestPlanMapper testPlanMapper;

    public void batchUpdateExecutor(TestPlanCaseUpdateRequest request) {
        try {
            List<String> ids = testPlanFunctionalCaseService.doSelectIds(request);
            if (CollectionUtils.isNotEmpty(ids)) {
                TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
                Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
                TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
                example.createCriteria().andIdIn(ids);
                List<TestPlanFunctionalCase> planCaseList = testPlanFunctionalCaseMapper.selectByExample(example);
                Map<String, String> userMap = planCaseList.stream().collect(Collectors.toMap(TestPlanFunctionalCase::getId, TestPlanFunctionalCase::getExecuteUser));
                Map<String, String> idsMap = planCaseList.stream().collect(Collectors.toMap(TestPlanFunctionalCase::getId, TestPlanFunctionalCase::getFunctionalCaseId));
                List<String> caseIds = planCaseList.stream().map(TestPlanFunctionalCase::getFunctionalCaseId).collect(Collectors.toList());
                FunctionalCaseExample caseExample = new FunctionalCaseExample();
                caseExample.createCriteria().andIdIn(caseIds);
                List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(caseExample);
                Map<String, String> caseMap = functionalCases.stream().collect(Collectors.toMap(FunctionalCase::getId, FunctionalCase::getName));
                List<LogDTO> dtoList = new ArrayList<>();
                idsMap.forEach((k, v) -> {
                    LogDTO dto = new LogDTO(
                            project.getId(),
                            project.getOrganizationId(),
                            k,
                            request.getUserId(),
                            OperationLogType.UPDATE.name(),
                            OperationLogModule.TEST_PLAN,
                            Translator.get("test_plan.update.executor") + ":" + caseMap.get(v));
                    dto.setPath("/test-plan/functional/case/batch/update/executor");
                    dto.setMethod(HttpMethodConstants.POST.name());
                    dto.setOriginalValue(JSON.toJSONBytes(userMap.get(k)));
                    dto.setModifiedValue(JSON.toJSONBytes(request.getUserId()));
                    dtoList.add(dto);
                });
                operationLogService.batchAdd(dtoList);
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }


    public void batchMove(BaseBatchMoveRequest request, String userId) {
        try {
            List<String> ids = testPlanFunctionalCaseService.doSelectIds(request);
            if (CollectionUtils.isNotEmpty(ids)) {
                TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
                Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
                TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
                example.createCriteria().andIdIn(ids);
                List<TestPlanFunctionalCase> caseList = testPlanFunctionalCaseMapper.selectByExample(example);
                List<String> functionalCaseIds = caseList.stream().map(TestPlanFunctionalCase::getFunctionalCaseId).collect(Collectors.toList());
                FunctionalCaseExample caseExample = new FunctionalCaseExample();
                caseExample.createCriteria().andIdIn(functionalCaseIds);
                List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(caseExample);
                Map<String, String> caseMap = functionalCases.stream().collect(Collectors.toMap(FunctionalCase::getId, FunctionalCase::getName));
                List<LogDTO> dtoList = new ArrayList<>();
                caseList.forEach(item -> {
                    LogDTO dto = new LogDTO(
                            project.getId(),
                            project.getOrganizationId(),
                            item.getFunctionalCaseId(),
                            userId,
                            OperationLogType.UPDATE.name(),
                            OperationLogModule.TEST_PLAN,
                            Translator.get("move") + ":" + caseMap.get(item.getFunctionalCaseId()));
                    dto.setPath("/test-plan/functional/case/batch/move");
                    dto.setMethod(HttpMethodConstants.POST.name());
                    dto.setOriginalValue(JSON.toJSONBytes(item));
                    TestPlanFunctionalCase testPlanFunctionalCase = new TestPlanFunctionalCase();
                    testPlanFunctionalCase.setId(item.getId());
                    testPlanFunctionalCase.setTestPlanCollectionId(request.getTargetCollectionId());
                    dto.setModifiedValue(JSON.toJSONBytes(testPlanFunctionalCase));
                    dtoList.add(dto);
                });
                operationLogService.batchAdd(dtoList);
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

}



