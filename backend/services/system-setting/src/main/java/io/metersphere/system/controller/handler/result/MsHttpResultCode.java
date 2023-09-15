package io.metersphere.system.controller.handler.result;

import io.metersphere.sdk.exception.IResultCode;

/**
 * 具有 Http 含义的状态码
 * 抛异常时使用，会将 http 状态码设置成当前的状态码的后三位数字
 * @author jianxing
 */
public enum MsHttpResultCode implements IResultCode {

    SUCCESS(100200, "http_result_success"),
    FAILED(100500, "http_result_unknown_exception"),
    VALIDATE_FAILED(100400, "http_result_validate"),
    UNAUTHORIZED(100401, "http_result_unauthorized"),
    FORBIDDEN(100403, "http_result_forbidden"),
    NOT_FOUND(100404, "http_result_not_found");

    private int code;
    private String message;

    MsHttpResultCode(int code, String message) {
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
