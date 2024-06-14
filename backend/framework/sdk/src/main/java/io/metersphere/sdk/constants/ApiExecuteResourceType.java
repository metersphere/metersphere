package io.metersphere.sdk.constants;

/**
 * 接口执行时的资源类型
 * @Author: jianxing
 * @CreateTime: 2023-12-08  10:53
 */
public enum ApiExecuteResourceType {
    API_DEBUG,
    API,
    API_CASE,
    API_SCENARIO,
    TEST_PLAN_API_CASE,
    TEST_PLAN_API_SCENARIO,
    /**
     * 测试计划整体执行-接口用例
     */
    PLAN_RUN_API_CASE,
    /**
     * 测试计划整体执行-场景用例
     */
    PLAN_RUN_API_SCENARIO
}
