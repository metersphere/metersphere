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
     * 默认 application/text
     */
    STRING("string"),
    /**
     * 整型
     * 默认 application/text
     */
    INTEGER("integer"),
    /**
     * 数值型
     * 默认 application/text
     */
    NUMBER("number"),
    /**
     * 数组
     * 默认 application/text
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
