package io.metersphere.system.dto.taskcenter.enums;

import java.util.List;

/**
 * @author: LAN
 * @date: 2024/1/22 16:41
 * @version: 1.0
 */
public enum ScheduleTagType {
    API_IMPORT("API_IMPORT"),
    TEST_RESOURCE("API_SCENARIO", "UI_SCENARIO", "LOAD_TEST", "TEST_PLAN"),

    ORDER("CLEAN_REPORT", "BUG_SYNC");

    private List<String> names;

    ScheduleTagType(String... names) {
        this.names = List.of(names);
    }

    public List<String> getNames() {
        return names;
    }

}
