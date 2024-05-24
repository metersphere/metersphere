package io.metersphere.api.dto.scenario;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ApiScenarioReportDetailBlobDTO implements Serializable {
    private String id;

    private String reportId;

    private String stepId;

    private String status;

    private String fakeCode;

    private String requestName;

    private Long requestTime;

    private String code;

    private Long responseSize;

    private String scriptIdentifier;

    private Long sort;

    private byte[] content;

    @Serial
    private static final long serialVersionUID = 1L;

}