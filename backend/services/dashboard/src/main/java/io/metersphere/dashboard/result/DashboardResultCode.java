package io.metersphere.dashboard.result;

import io.metersphere.sdk.exception.IResultCode;

/**
 * @author jianxing
 */
public enum DashboardResultCode implements IResultCode {

    NO_PROJECT_PERMISSION(109001, "no_project_permission");

    private final int code;
    private final String message;

    DashboardResultCode(int code, String message) {
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
