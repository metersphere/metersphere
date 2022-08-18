package io.metersphere.track.constants;

public enum TestCaseExportOtherField {
    VERSION("version"),
    COMMEND("commend"),
    EXECUTE_RESULT("executeResult"),
    REVIEW_RESULT("reviewResult"),
    CREATOR("creator"),
    CREATE_TIME("createTime"),
    UPDATE_TIME("updateTime");

    private String value;

    TestCaseExportOtherField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
