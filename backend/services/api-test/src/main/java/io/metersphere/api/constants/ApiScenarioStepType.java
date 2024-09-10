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
    API(StepTypeGroup.REQUEST),
    /**
     * 接口用例
     */
    API_CASE(StepTypeGroup.REQUEST),
    /**
     * 自定义请求
     */
    CUSTOM_REQUEST(StepTypeGroup.REQUEST),
    /**
     * 场景
     */
    API_SCENARIO(StepTypeGroup.SCENARIO),
    /**
     * 循环控制器
     */
    LOOP_CONTROLLER(StepTypeGroup.CONTROLLER),
    /**
     * 条件控制器
     */
    IF_CONTROLLER(StepTypeGroup.CONTROLLER),
    /**
     * 一次控制器
     */
    ONCE_ONLY_CONTROLLER(StepTypeGroup.CONTROLLER),
    /**
     * 等待控制器
     */
    CONSTANT_TIMER(StepTypeGroup.REQUEST),
    /**
     * 脚本操作
     */
    SCRIPT(StepTypeGroup.REQUEST),

    /**
     * JMeter插件
     */
    JMETER_COMPONENT(StepTypeGroup.REQUEST);


    private enum StepTypeGroup {
        REQUEST, CONTROLLER, SCENARIO
    }

    private StepTypeGroup stepTypeGroup;

    ApiScenarioStepType(StepTypeGroup stepTypeGroup) {
        this.stepTypeGroup = stepTypeGroup;
    }

    public Boolean isRequest() {
        return this.stepTypeGroup.equals(StepTypeGroup.REQUEST);
    }
}
