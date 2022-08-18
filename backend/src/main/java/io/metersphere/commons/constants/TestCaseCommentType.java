package io.metersphere.commons.constants;

public enum TestCaseCommentType {
    CASE("test_case_comment"),
    REVIEW("test_case_plan_comment"),
    PLAN("test_case_review_comment");

    private String i18nKey;

    TestCaseCommentType(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }
}
