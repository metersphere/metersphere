package io.metersphere.api.controller.result;

import io.metersphere.sdk.exception.IResultCode;

/**
 * @author jianxing
 */
public enum ApiResultCode implements IResultCode {

    API_DEBUG_EXIST(104001, "api_debug_exist"),
    API_DEFINITION_EXIST(104002, "api_definition_exist"),
    API_DEFINITION_NOT_EXIST(104003, "resource_not_exist");


    private final int code;
    private final String message;

    ApiResultCode(int code, String message) {
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
