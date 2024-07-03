package io.metersphere.sdk.constants;

/**
 * 结果状态
 */
public enum ResultStatus {
    /**
     * 成功
     */
    SUCCESS,

    /**
     * 错误
     */
    ERROR,

    /**
     * 误报
     */
    FAKE_ERROR,
    /**
     * 阻塞
     */
    BLOCKED,
}