package io.metersphere.api.dto.request.http;

import io.metersphere.sdk.constants.ValueEnum;

/**
 * 键值对参数的参数类型
 * rest 参数和 query 参数
 * @Author: jianxing
 * @CreateTime: 2024-01-27  11:22
 */
public enum KeyValueParamType implements ValueEnum {
    /**
     * 字符串类型
     */
    STRING("string"),
    /**
     * 整型
     */
    INTEGER("integer"),
    /**
     * 数值型
     */
    NUMBER("number"),
    /**
     * 布尔类型
     */
    BOOLEAN("boolean"),
    /**
     * 数组
     */
    ARRAY("array");

    private String value;

    KeyValueParamType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
