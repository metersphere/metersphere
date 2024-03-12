package io.metersphere.plugin.platform.enums;

import lombok.Getter;

@Getter
public enum PlatformCustomFieldType {

    /**
     * 输入框
     */
    INPUT(false),
    /**
     * 文本框
     */
    TEXTAREA(false),
    /**
     * 单选下拉框框
     */
    SELECT(true),
    /**
     * 多选下拉框框
     */
    MULTIPLE_SELECT(true),
    /**
     * 单选框
     */
    RADIO(true),
    /**
     * 复选框
     */
    CHECKBOX(true),
    /**
     * 单选成员
     */
    MEMBER(true),
    /**
     * 多选成员
     */
    MULTIPLE_MEMBER(true),
    /**
     * 日期
     */
    DATE(false),
    /**
     * 日期时间
     */
    DATETIME(false),
    /**
     * 整型
     */
    INT(false),
    /**
     * 浮点型
     */
    FLOAT(false),
    /**
     * 多值输入框（标签输入框）
     */
    MULTIPLE_INPUT(false),

    // (第三方平台单独的自定义类型)
    /**
     * 级联选择
     */
    CASCADER(true),
    /**
     * 富文本
     */
    RICH_TEXT(false);

    private final Boolean hasOption;

    PlatformCustomFieldType(Boolean hasOption) {
        this.hasOption = hasOption;
    }
}
