package io.metersphere.track.constants;

public enum TestCaseStatus {
    Prepare("test_case_status_prepare"),
    Underway("test_case_status_running"),
    Completed("test_case_status_finished");

    private String i18nKey;

    TestCaseStatus(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }
}
