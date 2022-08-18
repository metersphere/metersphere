package io.metersphere.excel.converter;

import io.metersphere.commons.constants.TestReviewCaseStatus;
import io.metersphere.track.dto.TestCaseDTO;

import java.util.HashMap;
import java.util.Map;

public class TestCaseExportReviewResultConverter implements TestCaseExportConverter {

    private Map<String, String> reviewCaseStatusMap = new HashMap<>();

    public TestCaseExportReviewResultConverter() {
        reviewCaseStatusMap.put(TestReviewCaseStatus.Prepare.name(), "test_case_status_prepare");
        reviewCaseStatusMap.put(TestReviewCaseStatus.Pass.name(), "execute_pass");
        reviewCaseStatusMap.put(TestReviewCaseStatus.UnPass.name(), "execute_not_pass");
    }

    @Override
    public String parse(TestCaseDTO testCase) {
        return getFromMapOfNullableWithTranslate(reviewCaseStatusMap, testCase.getReviewStatus());
    }
}
