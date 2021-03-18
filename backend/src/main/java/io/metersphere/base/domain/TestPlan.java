package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlan implements Serializable {
    private String id;

    private String workspaceId;

    private String reportId;

    private String name;

    private String description;

    private String status;

    private String stage;

    private String principal;

    private String testCaseMatchRule;

    private String executorMatchRule;

    private Long createTime;

    private Long updateTime;

    private Long actualEndTime;

    private Long plannedStartTime;

    private Long plannedEndTime;

    private Long actualStartTime;

    private String creator;

    private String projectId;

    private Integer executionTimes;

    private String tags;

    private static final long serialVersionUID = 1L;
}