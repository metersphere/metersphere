package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiScenarioReportStructure implements Serializable {
    private String id;

    private String reportId;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}