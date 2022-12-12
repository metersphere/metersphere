package io.metersphere.excel.converter;

public enum TestReviewCaseStatus {

    Pass("execute_pass", 1),
    UnPass("execute_not_pass", 2),
    Underway("test_case_review_status_underway", 3),
    Again("test_case_status_again", 4),
    Prepare("test_case_status_prepare", 5);

    private String i18nKey;
    private Integer order;

    TestReviewCaseStatus(String i18nKey, int order) {
        this.i18nKey = i18nKey;
        this.order = order;
    }

    public String getI18nKey() {
        return i18nKey;
    }
    public Integer getOrder() {
        return order;
    }
}
