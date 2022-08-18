package io.metersphere.commons.constants;

public enum TestPlanTestCaseStatus {
    Prepare("test_case_status_prepare"),
    Pass("execute_pass"),
    Failure("test_case_status_error"),
    Blocking("plan_case_status_blocking"),
    Skip("plan_case_status_skip"),
    Underway("test_case_status_prepare");

    private String i18nKey;

    TestPlanTestCaseStatus(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }
}
