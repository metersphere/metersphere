package io.metersphere.sdk.constants;

/**
 * 执行状态
 */
public enum ExecStatus {
    /**
     * 未执行
     */
    PENDING,
    /**
     * 执行中
     */
    RUNNING,
    /**
     * 重新执行中
     */
    RERUNNING,
    /**
     * 已停止
     */
    STOPPED,
    /**
     * 完成
     */
    COMPLETED
}
