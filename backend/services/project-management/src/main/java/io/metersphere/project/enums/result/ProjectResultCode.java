package io.metersphere.project.enums.result;

import io.metersphere.sdk.exception.IResultCode;

/**
 * @author guoyuqi
 */
public enum ProjectResultCode implements IResultCode {

    /**
     * 项目管理-消息设置-保存消息设置-所选用户不在当前系统中，会返回
     */
    SAVE_MESSAGE_TASK_USER_NO_EXIST(102001, "save_message_task_user_no_exist");

    private final int code;
    private final String message;

    ProjectResultCode(int code, String message) {
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
