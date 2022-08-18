package io.metersphere.excel.converter;

import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.track.dto.TestCaseDTO;

import java.util.HashMap;
import java.util.Map;

public class TestCaseExportExecuteResultConverter implements TestCaseExportConverter {

    private Map<String, String> planCaseStatusMap = new HashMap<>();

    public TestCaseExportExecuteResultConverter() {
        planCaseStatusMap.put(TestPlanTestCaseStatus.Pass.name(), "execute_pass");
        planCaseStatusMap.put(TestPlanTestCaseStatus.Underway.name(), "test_case_status_prepare");
        planCaseStatusMap.put(TestPlanTestCaseStatus.Blocking.name(), "plan_case_status_blocking");
        planCaseStatusMap.put(TestPlanTestCaseStatus.Failure.name(), "test_case_status_error");
        planCaseStatusMap.put(TestPlanTestCaseStatus.Skip.name(), "plan_case_status_skip");
    }

    @Override
    public String parse(TestCaseDTO testCase) {
        return getFromMapOfNullableWithTranslate(planCaseStatusMap, testCase.getLastExecuteResult());
    }
}
