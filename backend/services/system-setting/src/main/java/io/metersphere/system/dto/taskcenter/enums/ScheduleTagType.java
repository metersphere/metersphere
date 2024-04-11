package io.metersphere.system.dto.taskcenter.enums;

import java.util.List;

/**
 * @author: LAN
 * @date: 2024/1/22 16:41
 * @version: 1.0
 */
public enum ScheduleTagType {
    API_IMPORT("API_IMPORT"),
    API_SCENARIO("API_SCENARIO"),
    UI_SCENARIO("UI_TEST"),
    LOAD_TEST("LOAD_TEST"),
    TEST_PLAN("TEST_PLAN");

    private List<String> names;

    ScheduleTagType(String... names) {
        this.names = List.of(names);
    }

    public List<String> getNames() {
        return names;
    }

}
