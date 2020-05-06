package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlan implements Serializable {
    private String id;

    private String projectId;

    private String workspaceId;

    private String name;

    private String description;

    private String status;

    private String stage;

    private String principal;

    private String testCaseMatchRule;

    private String executorMatchRule;

    private Long createTime;

    private Long updateTime;

    private String tags;

    private static final long serialVersionUID = 1L;
}