package io.metersphere.functional.excel.constants;

public enum FunctionalCaseExportOtherField {

    CREATE_USER("create_user"),
    CREATE_TIME("create_time"),
    UPDATE_USER("update_user"),
    UPDATE_TIME("update_time"),
    REVIEW_STATUS("review_status"),
    LAST_EXECUTE_RESULT("last_execute_result"),
    CASE_COMMENT("case_comment"),
    EXECUTE_COMMENT("execute_comment"),
    REVIEW_COMMENT("review_comment");

    private String value;

    FunctionalCaseExportOtherField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
