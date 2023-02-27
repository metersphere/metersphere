package io.metersphere.plan.enums;

public enum FunctionCaseExecResult {
    PREPARE("Prepare"),
    SUCCESS("Pass"),
    ERROR("Failure"),
    BLOCKING("Blocking"),
    SKIP("Skip");
    String value;

    FunctionCaseExecResult(String value) {
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
