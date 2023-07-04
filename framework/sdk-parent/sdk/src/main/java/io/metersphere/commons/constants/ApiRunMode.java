package io.metersphere.commons.constants;

import org.apache.commons.lang3.StringUtils;

public enum ApiRunMode {
    RUN,
    DEBUG,
    DEFINITION,
    TEST_CASE,
    SCENARIO,
    API_PLAN,
    JENKINS_API_PLAN,
    JENKINS_SCENARIO_PLAN,
    JENKINS_PERFORMANCE_TEST,
    JENKINS,
    TEST_PLAN_PERFORMANCE_TEST,
    SCENARIO_PLAN,
    API,
    SCHEDULE_API_PLAN,
    SCHEDULE_SCENARIO,
    SCHEDULE_SCENARIO_PLAN,
    SCHEDULE_PERFORMANCE_TEST,
    MANUAL_PLAN,
    UI_SCENARIO,
    UI_SCENARIO_PLAN,
    UI_SCHEDULE_SCENARIO_PLAN,
    UI_JENKINS_SCENARIO_PLAN,
    UI_SCHEDULE_SCENARIO,
    DEFAULT;

    public static ApiRunMode fromString(String mode) {
        if (StringUtils.isNotBlank(mode)) {
            for (ApiRunMode runMode : ApiRunMode.values()) {
                if (runMode.name().equalsIgnoreCase(mode)) {
                    return runMode;
                }
            }
        }
        return DEFAULT;
    }
}
