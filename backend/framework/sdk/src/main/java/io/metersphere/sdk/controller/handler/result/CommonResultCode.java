package io.metersphere.sdk.controller.handler.result;

/**
 * 通用功能状态码
 * 通用功能返回的状态码
 * @author jianxing
 */
public enum CommonResultCode implements IResultCode {

    PLUGIN_GET_INSTANCE(100001, "get_plugin_instance_error");

    private int code;
    private String message;

    CommonResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
