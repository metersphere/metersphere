package io.metersphere.base.domain;

import java.io.Serializable;

public class TestPlan implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String description;

    private String status;

    private String testCaseMatchRule;

    private String executorMatchRule;

    private Long createTime;

    private Long updateTime;

    private String tags;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId == null ? null : projectId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getTestCaseMatchRule() {
        return testCaseMatchRule;
    }

    public void setTestCaseMatchRule(String testCaseMatchRule) {
        this.testCaseMatchRule = testCaseMatchRule == null ? null : testCaseMatchRule.trim();
    }

    public String getExecutorMatchRule() {
        return executorMatchRule;
    }

    public void setExecutorMatchRule(String executorMatchRule) {
        this.executorMatchRule = executorMatchRule == null ? null : executorMatchRule.trim();
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags == null ? null : tags.trim();
    }
}