package io.metersphere.bug.controller.result;


import io.metersphere.sdk.exception.IResultCode;

public enum BugMgtResultCode implements IResultCode {

    BUG_EXIST_EXCEPTION(108001, "bug_exists");

    private int code;
    private String message;

    BugMgtResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return getTranslationMessage(this.message);
    }
}
