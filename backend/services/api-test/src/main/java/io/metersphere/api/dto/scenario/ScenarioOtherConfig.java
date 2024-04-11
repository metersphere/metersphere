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
    private Boolean enableCookieShare = false;
    /**
     * 启用场景步骤等待时间
     */
    private Boolean enableStepWait = false;
    /**
     * 失败策略
     * @see FailureStrategy
     */
    private String failureStrategy = FailureStrategy.CONTINUE.name();

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
