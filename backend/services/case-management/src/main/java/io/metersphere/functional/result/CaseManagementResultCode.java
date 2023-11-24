package io.metersphere.functional.result;

import io.metersphere.sdk.exception.IResultCode;

public enum CaseManagementResultCode implements IResultCode {

    FUNCTIONAL_CASE_NOT_FOUND(105001, "case_comment.case_is_null"),

    CASE_REVIEW_NOT_FOUND(105001, "case_review.not.exist");

    private final int code;
    private final String message;

    CaseManagementResultCode(int code, String message) {
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
