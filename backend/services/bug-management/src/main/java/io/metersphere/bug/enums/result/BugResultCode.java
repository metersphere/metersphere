package io.metersphere.bug.enums.result;

import io.metersphere.sdk.exception.IResultCode;

/**
 * @author song-cc-rock
 */

public enum BugResultCode implements IResultCode {

    /**
     * 缺陷不存在
     */
    BUG_NOT_EXIST(108001, "bug_not_exist"),

    /**
     * 非Local缺陷异常
     */
    NOT_LOCAL_BUG_ERROR(108002, "not_local_bug_error");

    private final int code;
    private final String message;

    BugResultCode(int code, String message) {
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
