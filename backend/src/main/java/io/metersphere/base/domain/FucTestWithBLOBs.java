package io.metersphere.base.domain;

import java.io.Serializable;

public class FucTestWithBLOBs extends FucTest implements Serializable {
    private String runtimeConfiguration;

    private String schedule;

    private static final long serialVersionUID = 1L;

    public String getRuntimeConfiguration() {
        return runtimeConfiguration;
    }

    public void setRuntimeConfiguration(String runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration == null ? null : runtimeConfiguration.trim();
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule == null ? null : schedule.trim();
    }
}