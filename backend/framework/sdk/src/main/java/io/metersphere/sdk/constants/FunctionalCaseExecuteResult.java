package io.metersphere.sdk.constants;

public enum FunctionalCaseExecuteResult {

    /**
     * 未执行
     */
    PENDING,
    /**
     * 通过
     */
    SUCCESS,
    /**
     * 失败
     */
    ERROR,
    /**
     * 阻塞
     */
    BLOCKED,
    /**
     * 误报
     */
    FAKE_ERROR
}
