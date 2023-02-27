package io.metersphere.plan.dto;

import io.metersphere.base.domain.TestCaseTest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestCaseRelevanceCasesRequest {
    private String testCaseId;
    private String testPlanId;

    List<TestCaseTest> testCaseTestList;

    //以下四个自动化用例执行结果map，key-> testCaseId
    Map<String, CaseExecResult> apiAllCaseMap;
    Map<String, CaseExecResult> scenarioAllCaseMap;
    Map<String, CaseExecResult> loadAllCaseMap;
    Map<String, CaseExecResult> uiAllCaseMap;

}
