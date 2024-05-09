package io.metersphere.sdk.dto.api.result;

import lombok.Data;

/**
 * 断言结果
 */
@Data
public class ResponseAssertionResult {

    /**
     * 断言名称
     */
    private String name;

    /**
     * 断言内容
     */
    @Deprecated
    private String content;

    /**
     * 断言脚本
     */
    private String script;

    /**
     * 实际值
     */
    private String actualValue;

    /**
     * 期望值
     */
    private String expectedValue;

    /**
     * 断言类型
     * {@link AssertionResultType}
     */
    private String assertionType;

    /**
     * 断言条件
     */
    private String condition;

    /**
     * 断言结果
     */
    private String message;

    /**
     * 是否通过
     */
    private boolean pass;

    public enum AssertionResultType {
        DOCUMENT,
        RESPONSE_CODE,
        RESPONSE_HEADER,
        RESPONSE_TIME,
        SCRIPT,
        VARIABLE,
        JSON_PATH,
        XPATH,
        REGEX
    }
}
