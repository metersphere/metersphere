package io.metersphere.functional.result;

import io.metersphere.sdk.exception.IResultCode;

public enum FunctionalCaseResultCode implements IResultCode {

    FUNCTIONAL_CASE_NOT_FOUND(105001, "functional_case_not_exist");

    private int code;
    private String message;

    FunctionalCaseResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
