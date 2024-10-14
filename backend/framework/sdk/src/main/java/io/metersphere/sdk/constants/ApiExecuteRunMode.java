package io.metersphere.sdk.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * 接口执行时的资源类型
 *
 * @Author: jianxing
 * @CreateTime: 2023-12-08  10:53
 */
public enum ApiExecuteRunMode {
    /**
     * 手动运行
     */
    RUN,
    /**
     * 前端调试
     */
    FRONTEND_DEBUG,
    /**
     * 后端调试
     */
    BACKEND_DEBUG,

    /**
     * jenkins 触发
     */
    JENKINS,
    /**
     * 定时任务
     */
    SCHEDULE;

    public static boolean isDebug(String runMode) {
        return StringUtils.equalsAny(runMode, ApiExecuteRunMode.FRONTEND_DEBUG.name(), ApiExecuteRunMode.BACKEND_DEBUG.name());
    }

    public static boolean isFrontendDebug(String runMode) {
        return StringUtils.equals(runMode, ApiExecuteRunMode.FRONTEND_DEBUG.name());
    }
}
