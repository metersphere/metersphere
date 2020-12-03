package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReportDetail implements Serializable {
    private String reportId;

    private String scenarioId;

    private byte[] content;

    private static final long serialVersionUID = 1L;
}