package io.metersphere.plan.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseCommentMapper;
import io.metersphere.base.mapper.TestCaseTestMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.utils.HttpHeaderUtils;
import io.metersphere.constants.TestCaseCommentType;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.CaseExecResult;
import io.metersphere.plan.dto.TestCaseRelevanceCasesRequest;
import io.metersphere.plan.enums.FunctionCaseExecResult;
import io.metersphere.plan.enums.TestCaseReleevanceType;
import io.metersphere.plan.utils.TestCaseSyncStatusUtil;
import io.metersphere.service.BaseUserService;
import io.metersphere.utils.BatchProcessingUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseSyncStatusService {

    @Resource
    private ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    private ExtTestPlanUiCaseMapper extTestPlanUiCaseMapper;
    @Resource
    private TestCaseTestMapper testCaseTestMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private TestCaseCommentMapper testCaseCommentMapper;

    //通过自动化用例的状态，获取最新的功能用例状态。
    public void getTestCaseStatusByTestPlanExecuteOver(List<PlanReportCaseDTO> testPlanCaseList, List<TestPlanApiDTO> apiAllCases, List<TestPlanScenarioDTO> scenarioAllCases, List<TestPlanLoadCaseDTO> loadAllCases, List<TestPlanUiScenarioDTO> uiAllCases) {
        List<TestCaseTest> testCaseTestList = this.selectTestPlanTestCaseByPlanReportCase(testPlanCaseList);
        Map<String, CaseExecResult> testCaseStatusByAutomationCase = TestCaseSyncStatusUtil.getTestCaseStatusByAutomationCaseRunResult(
                testPlanCaseList, testCaseTestList, apiAllCases, scenarioAllCases, loadAllCases, uiAllCases);
        TestCaseSyncStatusUtil.updateFunctionCaseStatusByAutomationExecResult(testPlanCaseList, testCaseStatusByAutomationCase);
    }

    private List<TestCaseTest> selectTestPlanTestCaseByPlanReportCase(List<PlanReportCaseDTO> testPlanCaseList) {
        if (CollectionUtils.isNotEmpty(testPlanCaseList)) {
            List<String> testCaseIdList = new ArrayList<>();
            testPlanCaseList.forEach(item -> {
                if (!testCaseIdList.contains(item.getCaseId())) {
                    testCaseIdList.add(item.getCaseId());
                }
            });
            return BatchProcessingUtil.selectTestCaseTestByPrimaryKey(testCaseIdList, testCaseTestMapper::selectByExample);
        } else {
            return new ArrayList<>();
        }

    }

    //测试计划执行完成后，通过自动化用例的状态，同步更改功能用例状态。
    public void syncStatusByTestPlanExecuteOver(String operator, String testPlanName, List<PlanReportCaseDTO> testPlanCaseList, List<TestPlanApiDTO> apiAllCaseList, List<TestPlanScenarioDTO> scenarioAllCaseList, List<TestPlanLoadCaseDTO> loadAllCaseList, List<TestPlanUiScenarioDTO> uiAllCaseList) {
        if (CollectionUtils.isNotEmpty(testPlanCaseList)) {
            List<TestCaseTest> testCaseTestList = this.selectTestPlanTestCaseByPlanReportCase(testPlanCaseList);
            Map<String, CaseExecResult> testCaseStatusByAutomationCase = TestCaseSyncStatusUtil.getTestCaseStatusByAutomationCaseRunResult(
                    testPlanCaseList, testCaseTestList, apiAllCaseList, scenarioAllCaseList, loadAllCaseList, uiAllCaseList);

            Map<String, CaseExecResult> successCaseMap = new HashMap<>();
            Map<String, CaseExecResult> errorCaseMap = new HashMap<>();
            Map<String, CaseExecResult> blockingCaseMap = new HashMap<>();
            for (PlanReportCaseDTO testPlanCase : testPlanCaseList) {
                String testCaseId = testPlanCase.getCaseId();
                if (testCaseStatusByAutomationCase.containsKey(testCaseId)) {
                    CaseExecResult execResult = testCaseStatusByAutomationCase.get(testCaseId);
                    testPlanCase.setStatus(execResult.getExecResult());
                    execResult.setTestCaseId(testCaseId);
                    if (StringUtils.equalsIgnoreCase(FunctionCaseExecResult.ERROR.toString(), execResult.getExecResult())) {
                        errorCaseMap.put(testPlanCase.getId(), execResult);
                    } else if (StringUtils.equalsIgnoreCase(FunctionCaseExecResult.BLOCKING.toString(), execResult.getExecResult())) {
                        blockingCaseMap.put(testPlanCase.getId(), execResult);
                    } else if (StringUtils.equalsIgnoreCase(FunctionCaseExecResult.SUCCESS.toString(), execResult.getExecResult())) {
                        successCaseMap.put(testPlanCase.getId(), execResult);
                    }
                }
            }
            if (MapUtils.isNotEmpty(successCaseMap)) {
                extTestPlanTestCaseMapper.updateExecResultByTestPlanCaseIdList(new ArrayList<>(successCaseMap.keySet()), FunctionCaseExecResult.SUCCESS.toString());
            }
            if (MapUtils.isNotEmpty(errorCaseMap)) {
                extTestPlanTestCaseMapper.updateExecResultByTestPlanCaseIdList(new ArrayList<>(errorCaseMap.keySet()), FunctionCaseExecResult.ERROR.toString());
            }
            if (MapUtils.isNotEmpty(blockingCaseMap)) {
                extTestPlanTestCaseMapper.updateExecResultByTestPlanCaseIdList(new ArrayList<>(blockingCaseMap.keySet()), FunctionCaseExecResult.BLOCKING.toString());
                this.addTestCaseComment(operator, testPlanName, blockingCaseMap, FunctionCaseExecResult.BLOCKING.toString());
            }
        }
    }

    public void checkAndUpdateFunctionCaseStatus(String testId) {
        TestPlanApiCase testPlanApiCase = null;
        TestPlanApiScenario testPlanApiScenario = null;
        TestPlanLoadCase testPlanLoadCase = null;
        TestPlanUiScenario testPlanUiScenario = null;

        testPlanApiCase = extTestPlanApiCaseMapper.selectBaseInfoById(testId);
        if (testPlanApiCase == null) {
            testPlanApiScenario = extTestPlanScenarioCaseMapper.selectBaseInfoById(testId);
        }
        if (ObjectUtils.allNull(testPlanApiCase, testPlanApiScenario)) {
            testPlanLoadCase = extTestPlanLoadCaseMapper.selectBaseInfoById(testId);
        }
        if (ObjectUtils.allNull(testPlanApiCase, testPlanApiScenario, testPlanLoadCase)) {
            testPlanUiScenario = extTestPlanUiCaseMapper.selectBaseInfoById(testId);
        }

        String automationCaseId = null, planId = null;
        String triggerCaseExecResult = null;
        if (testPlanApiCase != null) {
            automationCaseId = testPlanApiCase.getApiCaseId();
            planId = testPlanApiCase.getTestPlanId();
            triggerCaseExecResult = testPlanApiCase.getStatus();
        } else if (testPlanApiScenario != null) {
            automationCaseId = testPlanApiScenario.getApiScenarioId();
            planId = testPlanApiScenario.getTestPlanId();
            triggerCaseExecResult = testPlanApiScenario.getLastResult();
        } else if (testPlanLoadCase != null) {
            automationCaseId = testPlanLoadCase.getLoadCaseId();
            planId = testPlanLoadCase.getTestPlanId();
            triggerCaseExecResult = testPlanLoadCase.getStatus();
        } else if (testPlanUiScenario != null) {
            automationCaseId = testPlanUiScenario.getUiScenarioId();
            planId = testPlanUiScenario.getTestPlanId();
            triggerCaseExecResult = testPlanUiScenario.getLastResult();
        }

        if (StringUtils.isNoneEmpty(automationCaseId, planId, triggerCaseExecResult)) {
            this.updateFunctionCaseStatusByAutomationCaseId(automationCaseId, planId, triggerCaseExecResult);
        }
    }

    private void updateFunctionCaseStatusByAutomationCaseId(String automationCaseId, String testPlanId, String triggerCaseRunResult) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        if (testPlan != null && testPlan.getAutomaticStatusUpdate()) {
            HttpHeaderUtils.runAsUser(baseUserService.getUserDTO(testPlan.getCreator()));

            Set<String> testCaseIdSet = new HashSet<>();
            List<TestPlanTestCase> testPlanTestCaseList = extTestPlanTestCaseMapper.selectByAutomationCaseIdAndTestPlanId(automationCaseId, testPlanId);
            testPlanTestCaseList.forEach(item -> testCaseIdSet.add(item.getCaseId()));

            if (CollectionUtils.isNotEmpty(testCaseIdSet)) {
                TestCaseTestExample testCaseTestExample = new TestCaseTestExample();
                testCaseTestExample.createCriteria().andTestCaseIdIn(new ArrayList<>(testCaseIdSet));
                List<TestCaseTest> testCaseTestList = testCaseTestMapper.selectByExample(testCaseTestExample);
                Map<String, List<TestCaseTest>> testCaseTestMap = testCaseTestList.stream().collect(Collectors.groupingBy(TestCaseTest::getTestCaseId));

                for (Map.Entry<String, List<TestCaseTest>> entry : testCaseTestMap.entrySet()) {
                    TestCaseRelevanceCasesRequest request = this.generateRelevanceCasesRequest(testPlanId, entry.getKey(), entry.getValue());
                    CaseExecResult priorityResult = TestCaseSyncStatusUtil.getTestCaseExecResultByRelevance(request.getTestCaseTestList(),
                            request.getApiAllCaseMap(), request.getScenarioAllCaseMap(), request.getLoadAllCaseMap(), request.getUiAllCaseMap());
                    if (priorityResult == null) {
                        continue;
                    }

                    String automationCaseResult = null;
                    if (priorityResult != null && StringUtils.isNotEmpty(priorityResult.getExecResult())) {
                        if (StringUtils.equalsIgnoreCase(ApiReportStatus.ERROR.name(), priorityResult.getExecResult())) {
                            automationCaseResult = ApiReportStatus.ERROR.name();
                            priorityResult.setExecResult(FunctionCaseExecResult.ERROR.toString());
                        } else if (StringUtils.equalsIgnoreCase(ApiReportStatus.FAKE_ERROR.name(), priorityResult.getExecResult())) {
                            automationCaseResult = ApiReportStatus.FAKE_ERROR.name();
                            priorityResult.setExecResult(FunctionCaseExecResult.BLOCKING.toString());
                        } else if (StringUtils.equalsIgnoreCase(ApiReportStatus.SUCCESS.name(), priorityResult.getExecResult())) {
                            priorityResult.setExecResult(FunctionCaseExecResult.SUCCESS.toString());
                        }
                    }

                    //通过 triggerCaseRunResult(触发操作的用例的执行结果) 进行判断，会不会直接影响最终结果。如果是，在改变功能用例状态时也要增加一条评论。
                    extTestPlanTestCaseMapper.updateExecResultByTestCaseIdAndTestPlanId(entry.getKey(), testPlanId, priorityResult.getExecResult());
                    if (StringUtils.equalsIgnoreCase(triggerCaseRunResult, automationCaseResult) && !StringUtils.equalsIgnoreCase(triggerCaseRunResult, ApiReportStatus.SUCCESS.name())) {
                        this.addTestCaseComment(testPlan.getCreator(), testPlan.getName(), entry.getKey(), priorityResult.getCaseName(), FunctionCaseExecResult.BLOCKING.toString());
                    }
                }
            }

            HttpHeaderUtils.clearUser();
        }
    }

    private TestCaseRelevanceCasesRequest generateRelevanceCasesRequest(String testPlanId, String testCaseId, List<TestCaseTest> testCaseTestList) {
        TestCaseRelevanceCasesRequest request = new TestCaseRelevanceCasesRequest();
        request.setTestCaseId(testCaseId);
        request.setTestPlanId(testPlanId);
        request.setTestCaseTestList(testCaseTestList);

        List<String> relevanceApiCaseList = new ArrayList<>();
        List<String> relevanceScenarioList = new ArrayList<>();
        List<String> relevanceLoadCaseList = new ArrayList<>();
        List<String> relevanceUiList = new ArrayList<>();
        testCaseTestList.forEach(testCaseTest -> {
            if (StringUtils.equalsIgnoreCase(TestCaseReleevanceType.API_CASE.toString(), testCaseTest.getTestType())) {
                relevanceApiCaseList.add(testCaseTest.getTestId());
            } else if (StringUtils.equalsIgnoreCase(TestCaseReleevanceType.SCENARIO.toString(), testCaseTest.getTestType())) {
                relevanceScenarioList.add(testCaseTest.getTestId());
            } else if (StringUtils.equalsIgnoreCase(TestCaseReleevanceType.LOAD_CASE.toString(), testCaseTest.getTestType())) {
                relevanceLoadCaseList.add(testCaseTest.getTestId());
            } else if (StringUtils.equalsIgnoreCase(TestCaseReleevanceType.UI_AUTOMATION.toString(), testCaseTest.getTestType())) {
                relevanceUiList.add(testCaseTest.getTestId());
            }
        });
        if (CollectionUtils.isNotEmpty(relevanceApiCaseList)) {
            List<CaseExecResult> apiAllCaseList = extTestPlanApiCaseMapper.selectExecResult(testPlanId, relevanceApiCaseList);
            request.setApiAllCaseMap(TestCaseSyncStatusUtil.getExecResultMap(apiAllCaseList));
        }
        if (CollectionUtils.isNotEmpty(relevanceScenarioList)) {
            List<CaseExecResult> scenarioAllCaseList = extTestPlanScenarioCaseMapper.selectExecResult(testPlanId, relevanceScenarioList);
            request.setScenarioAllCaseMap(TestCaseSyncStatusUtil.getExecResultMap(scenarioAllCaseList));
        }
        if (CollectionUtils.isNotEmpty(relevanceLoadCaseList)) {
            List<CaseExecResult> loadAllCaseList = extTestPlanLoadCaseMapper.selectExecResult(testPlanId, relevanceLoadCaseList);
            request.setLoadAllCaseMap(TestCaseSyncStatusUtil.getExecResultMap(loadAllCaseList));
        }
        if (CollectionUtils.isNotEmpty(relevanceUiList)) {
            List<CaseExecResult> uiAllCaseList = extTestPlanUiCaseMapper.selectExecResult(testPlanId, relevanceUiList);
            request.setUiAllCaseMap(TestCaseSyncStatusUtil.getExecResultMap(uiAllCaseList));
        }
        return request;
    }

    private void addTestCaseComment(String commentCreateUser, String testPlanName, Map<String, CaseExecResult> caseExecResultMap, String commentStatus) {
        if (MapUtils.isNotEmpty(caseExecResultMap)) {
            for (Map.Entry<String, CaseExecResult> itemEntry : caseExecResultMap.entrySet()) {
                this.addTestCaseComment(commentCreateUser, testPlanName, itemEntry.getValue().getTestCaseId(), itemEntry.getValue().getCaseName(), commentStatus);
            }
        }
    }

    private void addTestCaseComment(String commentCreateUser, String testPlanName, String testCaseId, String caseName, String commentStatus) {
        if (StringUtils.isNoneBlank(testPlanName, testCaseId, commentStatus, caseName)) {
            String commentDesc = TestCaseSyncStatusUtil.generateCommentDesc(testPlanName, caseName, Translator.get("api_status_fake_error"));
            long systemTime = System.currentTimeMillis();
            TestCaseComment testCaseComment = new TestCaseComment();
            testCaseComment.setCaseId(testCaseId);
            testCaseComment.setId(UUID.randomUUID().toString());
            testCaseComment.setCreateTime(systemTime);
            testCaseComment.setUpdateTime(systemTime);
            testCaseComment.setStatus(commentStatus);
            testCaseComment.setType(TestCaseCommentType.PLAN.name());
            testCaseComment.setDescription(commentDesc);
            testCaseComment.setAuthor(commentCreateUser);
            testCaseCommentMapper.insert(testCaseComment);
        }
    }
}
