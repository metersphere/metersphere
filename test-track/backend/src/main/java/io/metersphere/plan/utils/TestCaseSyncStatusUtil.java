package io.metersphere.plan.utils;

import io.metersphere.base.domain.TestCaseTest;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.dto.*;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.CaseExecResult;
import io.metersphere.plan.enums.FunctionCaseExecResult;
import io.metersphere.plan.enums.TestCaseReleevanceType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestCaseSyncStatusUtil {

    public static Map<String, CaseExecResult> getTestCaseStatusByAutomationCaseRunResult(
            List<PlanReportCaseDTO> testPlanCaseList,
            List<TestCaseTest> testCaseTestList,
            List<TestPlanApiDTO> apiAllCaseList,
            List<TestPlanScenarioDTO> scenarioAllCaseList,
            List<TestPlanLoadCaseDTO> loadAllCaseList,
            List<TestPlanUiScenarioDTO> uiAllCaseList) {
        Map<String, CaseExecResult> testCaseResultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(testPlanCaseList) && CollectionUtils.isNotEmpty(testCaseTestList)) {


            Map<String, List<TestCaseTest>> testCaseTestMap = testCaseTestList.stream().collect(Collectors.groupingBy(TestCaseTest::getTestCaseId));
            Map<String, CaseExecResult> apiExecResultMap = TestCaseSyncStatusUtil.getApiExecResultMap(apiAllCaseList);
            Map<String, CaseExecResult> scenarioExecResultMap = TestCaseSyncStatusUtil.getScenarioExecResultMap(scenarioAllCaseList);
            Map<String, CaseExecResult> loadCaseExecResultMap = TestCaseSyncStatusUtil.getLoadExecResultMap(loadAllCaseList);
            Map<String, CaseExecResult> uiExecResultMap = TestCaseSyncStatusUtil.getUiExecResultMap(uiAllCaseList);

            for (PlanReportCaseDTO testPlanCase : testPlanCaseList) {
                if (testCaseTestMap.containsKey(testPlanCase.getCaseId())) {
                    List<TestCaseTest> relevanceList = testCaseTestMap.get(testPlanCase.getCaseId());
                    CaseExecResult priorityResult = getTestCaseExecResultByRelevance(relevanceList, apiExecResultMap, scenarioExecResultMap, loadCaseExecResultMap, uiExecResultMap);
                    if (priorityResult != null && StringUtils.isNotEmpty(priorityResult.getExecResult())) {
                        if (StringUtils.equalsIgnoreCase(ApiReportStatus.ERROR.name(), priorityResult.getExecResult())) {
                            priorityResult.setExecResult(FunctionCaseExecResult.ERROR.toString());
                        } else if (StringUtils.equalsIgnoreCase(ApiReportStatus.FAKE_ERROR.name(), priorityResult.getExecResult())) {
                            priorityResult.setExecResult(FunctionCaseExecResult.BLOCKING.toString());
                        } else if (StringUtils.equalsIgnoreCase(ApiReportStatus.SUCCESS.name(), priorityResult.getExecResult())) {
                            priorityResult.setExecResult(FunctionCaseExecResult.SUCCESS.toString());
                        }
                        testCaseResultMap.put(testPlanCase.getCaseId(), priorityResult);
                    }
                }
            }

        }

        return testCaseResultMap;
    }

    public static CaseExecResult getTestCaseExecResultByRelevance(List<TestCaseTest> testCaseTestList,
                                                                  Map<String, CaseExecResult> apiCaseExecResultMap,
                                                                  Map<String, CaseExecResult> scenarioExecResultMap,
                                                                  Map<String, CaseExecResult> loadCaseExecResultMap,
                                                                  Map<String, CaseExecResult> uiExecResultMap) {
        CaseExecResult testCaseExecResult = null;
        if (apiCaseExecResultMap == null) {
            apiCaseExecResultMap = new HashMap<>();
        }
        if (scenarioExecResultMap == null) {
            scenarioExecResultMap = new HashMap<>();
        }
        if (loadCaseExecResultMap == null) {
            loadCaseExecResultMap = new HashMap<>();
        }
        if (uiExecResultMap == null) {
            uiExecResultMap = new HashMap<>();
        }

        ArrayList<CaseExecResult> statusList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(testCaseTestList)) {
            for (TestCaseTest item : testCaseTestList) {
                String relevanceId = item.getTestId();
                String relevanceType = item.getTestType();
                if (StringUtils.equalsIgnoreCase(TestCaseReleevanceType.API_CASE.toString(), relevanceType)) {
                    CaseExecResult execResult = apiCaseExecResultMap.get(relevanceId);
                    if (execResult != null) {
                        statusList.add(execResult);
                    }
                } else if (StringUtils.equalsIgnoreCase(TestCaseReleevanceType.SCENARIO.toString(), relevanceType)) {
                    CaseExecResult execResult = scenarioExecResultMap.get(relevanceId);
                    if (execResult != null) {
                        statusList.add(execResult);
                    }
                } else if (StringUtils.equalsIgnoreCase(TestCaseReleevanceType.LOAD_CASE.toString(), relevanceType)) {
                    CaseExecResult execResult = loadCaseExecResultMap.get(relevanceId);
                    if (execResult != null) {
                        statusList.add(execResult);
                    }
                } else if (StringUtils.equalsIgnoreCase(TestCaseReleevanceType.UI_AUTOMATION.toString(), relevanceType)) {
                    CaseExecResult execResult = uiExecResultMap.get(relevanceId);
                    if (execResult != null) {
                        statusList.add(execResult);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(statusList)) {
            testCaseExecResult = getPriorityStatusFromCaseExecResult(statusList.toArray(new CaseExecResult[statusList.size()]));
        }
        return testCaseExecResult;
    }

    public static void updateFunctionCaseStatusByAutomationExecResult(List<PlanReportCaseDTO> testPlanCaseList, Map<String, CaseExecResult> testCaseStatusByAutomationCase) {
        if (CollectionUtils.isNotEmpty(testPlanCaseList) && MapUtils.isNotEmpty(testCaseStatusByAutomationCase)) {
            for (PlanReportCaseDTO dto : testPlanCaseList) {
                if (testCaseStatusByAutomationCase.containsKey(dto.getCaseId())) {
                    String status = testCaseStatusByAutomationCase.get(dto.getCaseId()).getExecResult();
                    dto.setStatus(status);
                }
            }
        }
    }

    public static Map<String, CaseExecResult> getExecResultMap(List<CaseExecResult> caseExecResultList) {
        if (CollectionUtils.isEmpty(caseExecResultList)) {
            return new HashMap<>();
        } else {
            Map<String, CaseExecResult> returnMap = new HashMap<>();
            caseExecResultList.forEach(item -> {
                String id = item.getId();
                if (returnMap.containsKey(id)) {
                    returnMap.put(id, getPriorityStatusFromCaseExecResult(item, returnMap.get(id)));
                } else {
                    returnMap.put(id, item);
                }
            });
            return returnMap;
        }
    }

    public static Map<String, CaseExecResult> getApiExecResultMap(List<TestPlanApiDTO> apiAllCaseList) {
        if (CollectionUtils.isEmpty(apiAllCaseList)) {
            return new HashMap<>();
        } else {
            Map<String, CaseExecResult> returnMap = new HashMap<>();
            apiAllCaseList.forEach(item -> {
                String id = item.getCaseId();
                CaseExecResult execResult = new CaseExecResult();
                execResult.setId(item.getId());
                execResult.setReportId(item.getReportId());
                execResult.setExecResult(item.getExecResult());
                execResult.setCaseName(item.getName());
                if (returnMap.containsKey(id)) {
                    returnMap.put(id, getPriorityStatusFromCaseExecResult(execResult, returnMap.get(id)));
                } else {
                    returnMap.put(id, execResult);
                }
            });
            return returnMap;
        }
    }

    public static Map<String, CaseExecResult> getScenarioExecResultMap(List<TestPlanScenarioDTO> scenarioAllCaseList) {
        if (CollectionUtils.isEmpty(scenarioAllCaseList)) {
            return new HashMap<>();
        } else {
            Map<String, CaseExecResult> returnMap = new HashMap<>();
            scenarioAllCaseList.forEach(item -> {
                String id = item.getCaseId();
                CaseExecResult execResult = new CaseExecResult();
                execResult.setId(item.getId());
                execResult.setReportId(item.getReportId());
                execResult.setExecResult(item.getLastResult());
                execResult.setCaseName(item.getName());
                if (returnMap.containsKey(id)) {
                    returnMap.put(id, getPriorityStatusFromCaseExecResult(execResult, returnMap.get(id)));
                } else {
                    returnMap.put(id, execResult);
                }
            });
            return returnMap;
        }
    }

    public static Map<String, CaseExecResult> getLoadExecResultMap(List<TestPlanLoadCaseDTO> loadAllCaseList) {
        if (CollectionUtils.isEmpty(loadAllCaseList)) {
            return new HashMap<>();
        } else {
            Map<String, CaseExecResult> returnMap = new HashMap<>();
            loadAllCaseList.forEach(item -> {
                String id = item.getLoadCaseId();
                CaseExecResult execResult = new CaseExecResult();
                execResult.setId(item.getId());
                execResult.setReportId(item.getReportId());
                execResult.setExecResult(item.getStatus());
                execResult.setCaseName(item.getName());
                if (returnMap.containsKey(id)) {
                    returnMap.put(id, getPriorityStatusFromCaseExecResult(execResult, returnMap.get(id)));
                } else {
                    returnMap.put(id, execResult);
                }
            });
            return returnMap;
        }
    }

    public static Map<String, CaseExecResult> getUiExecResultMap(List<TestPlanUiScenarioDTO> uiAllCaseList) {
        if (CollectionUtils.isEmpty(uiAllCaseList)) {
            return new HashMap<>();
        } else {
            Map<String, CaseExecResult> returnMap = new HashMap<>();
            uiAllCaseList.forEach(item -> {
                String id = item.getCaseId();
                CaseExecResult execResult = new CaseExecResult();
                execResult.setId(item.getId());
                execResult.setReportId(item.getReportId());
                execResult.setExecResult(item.getLastResult());
                execResult.setCaseName(item.getName());
                if (returnMap.containsKey(id)) {
                    returnMap.put(id, getPriorityStatusFromCaseExecResult(execResult, returnMap.get(id)));
                } else {
                    returnMap.put(id, execResult);
                }
            });
            return returnMap;
        }
    }

    //获取更高优先级的状态 error优先级最高，其次是fake_error。
    public static CaseExecResult getPriorityStatusFromCaseExecResult(CaseExecResult... execResults) {
        CaseExecResult errorStatus = null;
        CaseExecResult fakeErrorStatus = null;
        CaseExecResult successStatus = null;
        CaseExecResult lastNotNullStatus = null;
        boolean hasNoneStatus = false;
        for (CaseExecResult execResult : execResults) {
            if (StringUtils.equalsIgnoreCase(ApiReportStatus.ERROR.name(), execResult.getExecResult())) {
                errorStatus = execResult;
            } else if (StringUtils.equalsIgnoreCase(ApiReportStatus.FAKE_ERROR.name(), execResult.getExecResult())) {
                fakeErrorStatus = execResult;
            } else if (StringUtils.equalsAnyIgnoreCase(execResult.getExecResult(), ApiReportStatus.SUCCESS.name(), TestPlanStatus.Completed.name())) {
                successStatus = execResult;
            } else if (StringUtils.isEmpty(execResult.getExecResult())) {
                hasNoneStatus = true;
            } else {
                lastNotNullStatus = execResult;
            }
        }
        if (hasNoneStatus) {
            //存在未执行的状态，优先返回空
            return null;
        } else {
            return errorStatus != null ? errorStatus :
                    fakeErrorStatus != null ? fakeErrorStatus :
                            successStatus != null ? successStatus : lastNotNullStatus;
        }
    }

    public static String generateCommentDesc(String testPlanName, String caseName, String status) {
        return String.format("关联的case %s 在测试计划【%s】内的执行结果出现%s。", caseName, testPlanName, status);
    }
}
