package io.metersphere.sdk.constants;

/**
 * 执行任务的触发方式
 * @Author: jianxing
 * @CreateTime: 2024-02-06  11:54
 */
public enum TaskTriggerMode {
    /**
     * 定时任务
     */
    SCHEDULE,
    /**
     * 手动执行
     */
    MANUAL,
    /**
     * 接口调用
     */
    API,
    /**
     * 批量执行
     */
    BATCH
}
