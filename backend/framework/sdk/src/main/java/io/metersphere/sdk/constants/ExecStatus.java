package io.metersphere.sdk.constants;

/**
 * 报告执行状态
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
     * 重新执行
     */
    RERUNNING,
    /**
     * 已停止
     */
    STOPPED,
    /**
     * 已完成
     */
    COMPLETED
}
