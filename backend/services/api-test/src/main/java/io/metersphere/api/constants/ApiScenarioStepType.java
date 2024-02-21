package io.metersphere.api.constants;

/**
 * 步骤的类型
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
public enum ApiScenarioStepType {
    /**
     * 接口定义
     */
    API,
    /**
     * 接口用例
     */
    API_CASE,
    /**
     * 自定义请求
     */
    CUSTOM_REQUEST,
    /**
     * 场景
     */
    API_SCENARIO
    // todo 逻辑控制器等
}
