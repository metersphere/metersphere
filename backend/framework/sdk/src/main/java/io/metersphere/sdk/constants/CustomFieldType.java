package io.metersphere.sdk.constants;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum CustomFieldType {
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
    MULTIPLE_INPUT(false, "multipleInput");

    private final Boolean hasOption;

    private final String type;

    CustomFieldType(Boolean hasOption, String type) {
        this.hasOption = hasOption;
        this.type = type;
    }

    public Boolean getHasOption() {
        return this.hasOption;
    }

    public String getType() {
        return this.type;
    }

    public static Set<String> getHasOptionValueSet() {
        return Arrays.stream(CustomFieldType.values())
                .filter(CustomFieldType::getHasOption)
                .map(CustomFieldType::name)
                .collect(Collectors.toSet());
    }
}
