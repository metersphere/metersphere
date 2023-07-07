package io.metersphere.system.controller.result;

import io.metersphere.sdk.controller.handler.result.IResultCode;

/**
 * @author jianxing
 */
public enum SystemResultCode implements IResultCode {

    /**
     * 调用获取全局用户组接口，如果操作的是非全局的用户组，会返回该响应码
     */
    GLOBAL_USER_ROLE_PERMISSION(101001, "global_user_role_permission_error"),
    GLOBAL_USER_ROLE_EXIST(101002, "global_user_role_exist_error"),
    GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION(101003, "global_user_role_relation_system_permission_error")
    ;

    private final int code;
    private final String message;

    SystemResultCode(int code, String message) {
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
