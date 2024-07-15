package io.metersphere.api.dto.request.http.body;

/**
 * 请求体键值参数的参数类型
 * x-www-form-urlencoded 和 form-data
 * @Author: jianxing
 * @CreateTime: 2024-01-26  10:59
 */

import io.metersphere.sdk.constants.ValueEnum;

/**
 *
 */
public enum BodyParamType implements ValueEnum {
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
    BOOLEAN("boolean"),
    /**
     * 数组
     * 默认 application/text
     */
    ARRAY("array"),
    /**
     * 文件类型
     * 默认 application/octet-stream
     */
    FILE("file"),
    /**
     * json 类型
     * 默认 application/json
     */
    JSON("json");

    private String value;

    BodyParamType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
