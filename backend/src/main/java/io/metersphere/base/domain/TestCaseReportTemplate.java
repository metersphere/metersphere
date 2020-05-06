package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseReportTemplate implements Serializable {
    private Long id;

    private String name;

    private String workspaceId;

    private Long startTime;

    private Long endTime;

    private String content;

    private static final long serialVersionUID = 1L;
}