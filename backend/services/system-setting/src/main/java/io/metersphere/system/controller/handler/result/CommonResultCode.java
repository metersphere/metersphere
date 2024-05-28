package io.metersphere.system.controller.handler.result;

import io.metersphere.sdk.exception.IResultCode;

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
    INTERNAL_USER_ROLE_PERMISSION(100003, "internal_user_role_permission_error"),
    USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION(100004, "user_role_relation_remove_admin_user_permission_error"),
    FILE_NAME_ILLEGAL(100005, "file_name_illegal_error"),
    PLUGIN_ENABLE(100006, "plugin_enable_error"),
    PLUGIN_PERMISSION(100007, "plugin_permission_error"),
    INTERNAL_CUSTOM_FIELD_PERMISSION(100008, "internal_custom_field_permission_error"),
    INTERNAL_TEMPLATE_PERMISSION(100009, "internal_template_permission_error"),
    TEMPLATE_SCENE_ILLEGAL(100010, "template_scene_illegal_error"),
    CUSTOM_FIELD_EXIST(100012, "custom_field.exist"),
    TEMPLATE_EXIST(100013, "template.exist"),
    DEFAULT_TEMPLATE_PERMISSION(100014, "default_template_permission_error"),
    STATUS_ITEM_NOT_EXIST(100015, "status_item.not.exist"),
    STATUS_ITEM_EXIST(100016, "status_item.exist"),
    FIELD_VALIDATE_ERROR(100017, "simple_field_validate_error_tips"),
    STATUS_DEFINITION_REQUIRED_ERROR(100018, "status_definition_required_error"),
    ADMIN_USER_ROLE_PERMISSION(100019, "internal_admin_user_role_permission_error");


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
