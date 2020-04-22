package io.metersphere.base.domain;

import java.io.Serializable;

public class ApiTestWithBLOBs extends ApiTest implements Serializable {
    private String scenarioDefinition;

    private String schedule;

    private static final long serialVersionUID = 1L;

    public String getScenarioDefinition() {
        return scenarioDefinition;
    }

    public void setScenarioDefinition(String scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition == null ? null : scenarioDefinition.trim();
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule == null ? null : schedule.trim();
    }
}