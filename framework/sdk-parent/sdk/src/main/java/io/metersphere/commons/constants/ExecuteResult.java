package io.metersphere.commons.constants;

public enum ExecuteResult {
    //误报状态
    ERROR_REPORT_RESULT("errorReportResult"), ERROR_REPORT("errorReport"),
    //停止状态
    STOP("STOP"),
    //接口执行状态(兼容旧数据)
    API_SUCCESS("success"), API_ERROR("error"),
    //未执行状态
    UN_EXECUTE("unexecute"),
    //场景执行状态(兼容旧数据)
    SCENARIO_SUCCESS("Success"), SCENARIO_ERROR("Error"),
    //测试计划执行状态(兼容旧数据)
    TEST_PLAN_PREPARE("PREPARE"), TEST_PLAN_RUNNING("RUNNING"),

    FAIL("fail");

    private String value;

    ExecuteResult(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getValue() {
        return this.value;
    }
}
