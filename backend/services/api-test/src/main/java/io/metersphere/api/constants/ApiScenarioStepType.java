package io.metersphere.api.constants;

/**
 * 步骤的类型
 *
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
    API_SCENARIO,
    /**
     * 循环控制器
     */
    LOOP_CONTROLLER,
    /**
     * 条件控制器
     */
    IF_CONTROLLER,
    /**
     * 一次控制器
     */
    ONCE_ONLY_CONTROLLER,

}
