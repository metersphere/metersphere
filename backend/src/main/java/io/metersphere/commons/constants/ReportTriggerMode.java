package io.metersphere.commons.constants;

public enum ReportTriggerMode {
    MANUAL,
    SCHEDULE,
    API,
    /**
     * 性能测试用例执行触发报告
     */
    CASE,
    TEST_PLAN_SCHEDULE,
    TEST_PLAN_API,
}
