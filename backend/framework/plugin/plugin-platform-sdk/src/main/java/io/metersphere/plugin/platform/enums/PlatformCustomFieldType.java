package io.metersphere.plugin.platform.enums;

import lombok.Getter;

@Getter
public enum PlatformCustomFieldType {

    /**
     * 输入框
     */
    INPUT(false, "input"),
    /**
     * 文本框
     */
    TEXTAREA(false, "textarea"),
    /**
     * 单选下拉框框
     */
    SELECT(true, "select"),
    /**
     * 多选下拉框框
     */
    MULTIPLE_SELECT(true, "multipleSelect"),
    /**
     * 单选框
     */
    RADIO(true, "radio"),
    /**
     * 复选框
     */
    CHECKBOX(true, "checkbox"),
    /**
     * 单选成员
     */
    MEMBER(true, "member"),
    /**
     * 多选成员
     */
    MULTIPLE_MEMBER(true, "multipleMember"),
    /**
     * 日期
     */
    DATE(false, "date"),
    /**
     * 日期时间
     */
    DATETIME(false, "datetime"),
    /**
     * 整型
     */
    INT(false, "int"),
    /**
     * 浮点型
     */
    FLOAT(false, "float"),
    /**
     * 多值输入框（标签输入框）
     */
    MULTIPLE_INPUT(false, "multipleInput"),
    /**
     * 级联选择
     */
    CASCADE_SELECT(true, "cascadingSelect"),
    /**
     * 富文本
     */
    RICH_TEXT(false, "richText");

    private final Boolean hasOption;

    private final String type;

    PlatformCustomFieldType(Boolean hasOption, String type) {
        this.hasOption = hasOption;
        this.type = type;
    }
}
