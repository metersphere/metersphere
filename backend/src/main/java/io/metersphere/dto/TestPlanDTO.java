package io.metersphere.dto;

import io.metersphere.base.domain.TestPlan;

public class TestPlanDTO extends TestPlan {
    private String projectName;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
