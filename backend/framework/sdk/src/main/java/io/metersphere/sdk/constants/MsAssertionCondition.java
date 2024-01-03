package io.metersphere.sdk.constants;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-23  10:21
 */
public enum MsAssertionCondition {
    /**
     * 不校验
     */
    UNCHECK,
    /**
     * 包含
     */
    CONTAINS,
    /**
     * 不包含
     */
    NOT_CONTAINS,
    /**
     * 等于
     */
    EQUALS,
    /**
     * 不等于
     */
    NOT_EQUALS,
    /**
     * 大于
     */
    GT,
    /**
     * 大于或等于
     */
    GT_OR_EQUALS,
    /**
     * 小于
     */
    LT,
    /**
     * 小于或等于
     */
    LT_OR_EQUALS,
    /**
     * 以...开始
     */
    START_WITH,
    /**
     * 以...结束
     */
    END_WITH,
    /**
     * 为空
     */
    EMPTY,
    /**
     * 不为空
     */
    NOT_EMPTY,
    /**
     * 正则匹配
     */
    REGEX,
    /**
     * 长度等于
     */
    LENGTH_EQUALS,
    /**
     * 长度不等于
     */
    LENGTH_NOT_EQUALS,
    /**
     * 长度大于
     */
    LENGTH_GT,
    /**
     * 长度大于或等于
     */
    LENGTH_GT_OR_EQUALS,
    /**
     * 长度小于
     */
    LENGTH_LT,
    /**
     * 长度小于或等于
     */
    LENGTH_LT_OR_EQUALS,
}
