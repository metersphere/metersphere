package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class UiScenarioReportResult implements Serializable {
    private String id;

    private String resourceId;

    private String reportId;

    private Long createTime;

    private String status;

    private Long requestTime;

    private Long totalAssertions;

    private Long passAssertions;

    private String errorCode;

    private static final long serialVersionUID = 1L;
}