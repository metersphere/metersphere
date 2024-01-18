package io.metersphere.api.dto.scenario;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-12  09:47
 */
@Data
public class ScenarioOtherConfig {
    /**
     * 使用全局cookie
     */
    private Boolean enableGlobalCookie = true;
    /**
     * 是否共享cookie
     */
    private Boolean enableCookieShare;
    /**
     * 场景步骤等待时间
     * 每一个步骤执行后都会等待相应的时间
     */
    private Integer stepWaitTime;
    /**
     * 失败策略
     * @see FailureStrategy
     */
    private String failureStrategy;

    public enum FailureStrategy {
        /**
         * 继续执行
         */
        CONTINUE,
        /**
         * 停止执行
         */
        STOP
    }
}
