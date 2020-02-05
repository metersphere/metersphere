package io.metersphere.base.domain;

import java.io.Serializable;

public class LoadTestWithBLOBs extends LoadTest implements Serializable {
    private String loadConfiguration;

    private String advancedConfiguration;

    private String schedule;

    private static final long serialVersionUID = 1L;

    public String getLoadConfiguration() {
        return loadConfiguration;
    }

    public void setLoadConfiguration(String loadConfiguration) {
        this.loadConfiguration = loadConfiguration == null ? null : loadConfiguration.trim();
    }

    public String getAdvancedConfiguration() {
        return advancedConfiguration;
    }

    public void setAdvancedConfiguration(String advancedConfiguration) {
        this.advancedConfiguration = advancedConfiguration == null ? null : advancedConfiguration.trim();
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule == null ? null : schedule.trim();
    }
}