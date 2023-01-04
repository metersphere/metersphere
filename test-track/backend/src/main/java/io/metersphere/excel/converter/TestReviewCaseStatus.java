package io.metersphere.excel.converter;

public enum TestReviewCaseStatus {
    Prepare("test_case_status_prepare"),
    Again("test_case_status_again"),
    Pass("execute_pass"),
    UnPass("execute_not_pass");

    private String i18nKey;

    TestReviewCaseStatus(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }
}
