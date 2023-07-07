package io.metersphere.sdk.controller.handler.result;

/**
 * 通用功能状态码
 * 通用功能返回的状态码
 * @author jianxing
 */
public enum CommonResultCode implements IResultCode {

    PLUGIN_GET_INSTANCE(100001, "get_plugin_instance_error"),
    USER_ROLE_RELATION_EXIST(100002, "user_role_relation_exist_error"),
    /**
     * 调用获取全局用户组接口，如果操作的是内置的用户组，会返回该响应码
     */
    INTERNAL_USER_ROLE_PERMISSION(101003, "internal_user_role_permission_error"),
    USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION(100004, "user_role_relation_remove_admin_user_permission_error");

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
