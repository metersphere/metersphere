package io.metersphere.functional.excel.constants;

public enum FunctionalCaseExportOtherField {

    CREATE_USER("createUser"),
    CREATE_TIME("createTime"),
    UPDATE_USER("updateUser"),
    UPDATE_TIME("updateTime"),
    REVIEW_STATUS("reviewStatus"),
    LAST_EXECUTE_RESULT("lastExecuteResult"),
    CASE_COMMENT("caseComment"),
    EXECUTE_COMMENT("executeComment"),
    REVIEW_COMMENT("reviewComment");

    private String value;

    FunctionalCaseExportOtherField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
