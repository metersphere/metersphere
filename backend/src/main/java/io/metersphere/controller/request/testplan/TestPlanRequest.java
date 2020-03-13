package io.metersphere.controller.request.testplan;

public class TestPlanRequest {

    private String id;

    private String projectId;

    private String name;

    private String description;

    private String scenarioDefinition;

    private Long createTime;

    private Long updateTime;

    private String loadConfiguration;

    private String advancedConfiguration;

    private String runtimeConfiguration;

    private String schedule;

    private static final long serialVersionUID = 1L;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScenarioDefinition() {
        return scenarioDefinition;
    }

    public void setScenarioDefinition(String scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getRuntimeConfiguration() {
        return runtimeConfiguration;
    }

    public void setRuntimeConfiguration(String runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration;
    }

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
