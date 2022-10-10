package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiScenarioReportDetail implements Serializable {
    private String reportId;

    private String projectId;

    private byte[] content;

    private static final long serialVersionUID = 1L;
}