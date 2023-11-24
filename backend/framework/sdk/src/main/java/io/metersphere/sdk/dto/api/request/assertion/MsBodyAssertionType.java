package io.metersphere.sdk.dto.api.request.assertion;

/**
 * body断言中的断言类型
 */
public enum MsBodyAssertionType {
    /**
     * 正则断言
     */
    REGEX,
    /**
     * XPath断言
     */
    XPATH,
    /**
     * JSONPath断言
     */
    JSON_PATH,
    /**
     * 文档断言
     */
    DOCUMENT
}
