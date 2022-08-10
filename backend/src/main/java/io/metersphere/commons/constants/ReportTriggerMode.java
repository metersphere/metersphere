package io.metersphere.commons.constants;

public enum ReportTriggerMode {
    MANUAL,
    SCHEDULE,
    API,
    JENKINS_RUN_TEST_PLAN,
    /**
     * 性能测试用例执行触发报告
     */
    CASE,
    TEST_PLAN_SCHEDULE,
    TEST_PLAN_API,
    TEST_PLAN_MANUAL,
    API_PLAN,
    BATCH
}
