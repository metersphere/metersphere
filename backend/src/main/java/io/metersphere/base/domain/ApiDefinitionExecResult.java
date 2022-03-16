package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinitionExecResult implements Serializable {
    private String id;

    private String name;

    private String resourceId;

    private String status;

    private String userId;

    private Long startTime;

    private Long endTime;

    private Long createTime;

    private String type;

    private String actuator;

    private String triggerMode;

    private String errorCode;

    private String versionId;

    private String projectId;

    private String integratedReportId;

    private String reportType;

    private String content;

    private static final long serialVersionUID = 1L;
}