package io.metersphere.issue.controller.result;

import io.metersphere.sdk.controller.handler.result.IResultCode;

public enum IssueResultCode implements IResultCode {

    ISSUE_EXIST_EXCEPTION(108001, "issue_exists");

    private int code;
    private String message;

    IssueResultCode(int code, String message) {
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
