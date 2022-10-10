package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiTestReportDetail implements Serializable {
    private String reportId;

    private String testId;

    private byte[] content;

    private static final long serialVersionUID = 1L;
}