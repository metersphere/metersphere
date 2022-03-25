package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiScenarioReport implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private Long createTime;

    private Long updateTime;

    private String status;

    private String userId;

    private String triggerMode;

    private String executeType;

    private String scenarioName;

    private String scenarioId;

    private String createUser;

    private String actuator;

    private Long endTime;

    private Integer reportVersion;

    private String versionId;

    private String reportType;

    private String description;

    private static final long serialVersionUID = 1L;
}