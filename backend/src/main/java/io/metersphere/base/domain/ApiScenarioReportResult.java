package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReportResult implements Serializable {
    private String id;

    private String resourceId;

    private String reportId;

    private Long createTime;

    private String status;

    private Long requestTime;

    private Long totalAssertions;

    private Long passAssertions;

    private String errorCode;

    private byte[] content;

    private static final long serialVersionUID = 1L;
}