package io.metersphere.system.controller.result;

import io.metersphere.sdk.controller.handler.result.IResultCode;

/**
 * @author jianxing
 */
public enum SystemResultCode implements IResultCode {

    /**
     * 调用获取全局用户组接口，如果操作的是非全局的用户组，会返回该响应码
     */
    GLOBAL_USER_ROLE_PERMISSION(101001, "没有权限操作非全局用户组"),
    /**
     * 调用获取全局用户组接口，如果操作的是内置的用户组，会返回该响应码
     */
    INTERNAL_USER_ROLE_PERMISSION(101002, "内置用户组无法编辑与删除"),
    GLOBAL_USER_ROLE_EXIST(101003, "全局用户组已存在"),
    GLOBAL_USER_ROLE_RELATION_EXIST(101004, "用户已在当前用户组"),
    GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION(101005, "没有权限操作非系统级别用户组"),
    GLOBAL_USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION(101005, "无法将 admin 用户将系统管理员用户组删除")
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
