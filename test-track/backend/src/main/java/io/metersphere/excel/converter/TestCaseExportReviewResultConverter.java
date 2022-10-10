package io.metersphere.excel.converter;

import io.metersphere.dto.TestCaseDTO;

import java.util.HashMap;
import java.util.Map;

public class TestCaseExportReviewResultConverter implements TestCaseExportConverter {

    private Map<String, String> reviewCaseStatusMap = new HashMap<>();

    public TestCaseExportReviewResultConverter() {
        for (TestReviewCaseStatus value : TestReviewCaseStatus.values()) {
            reviewCaseStatusMap.put(value.name(), value.getI18nKey());
        }
    }

    @Override
    public String parse(TestCaseDTO testCase) {
        return getFromMapOfNullableWithTranslate(reviewCaseStatusMap, testCase.getReviewStatus());
    }
}
