package io.metersphere.excel.converter;

import io.metersphere.dto.TestCaseDTO;
import io.metersphere.excel.constants.TestPlanTestCaseStatus;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

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
        if (StringUtils.isBlank(testCase.getLastExecuteResult())) {
            return Translator.get("execute_result_prepare");
        }
        return getFromMapOfNullableWithTranslate(planCaseStatusMap, testCase.getLastExecuteResult());
    }
}
