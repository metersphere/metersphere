package io.metersphere.excel.converter;

import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.track.dto.TestCaseDTO;

import java.util.HashMap;
import java.util.Map;

public class TestCaseExportExecuteResultConverter implements TestCaseExportConverter {

    private Map<String, String> planCaseStatusMap = new HashMap<>();

    public TestCaseExportExecuteResultConverter() {
        for (TestPlanTestCaseStatus value : TestPlanTestCaseStatus.values()) {
            planCaseStatusMap.put(value.name(), value.getI18nKey());
        }
    }

    @Override
    public String parse(TestCaseDTO testCase) {
        return getFromMapOfNullableWithTranslate(planCaseStatusMap, testCase.getLastExecuteResult());
    }
}
