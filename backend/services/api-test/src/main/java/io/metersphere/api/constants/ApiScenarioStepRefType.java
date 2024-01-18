package io.metersphere.api.constants;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
public enum ApiScenarioStepRefType {
    /**
     * 在场景中直接创建的步骤
     * 例如 自定义请求，逻辑控制器
     */
    DIRECT,
    /**
     * 引用
     */
    REF,
    /**
     * 步骤引用
     */
    STEP_REF,
    /**
     * 复制
     */
    COPY
}
