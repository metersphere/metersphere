package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiScenarioReportResultWithBLOBs extends ApiScenarioReportResult implements Serializable {
    private byte[] content;

    private String baseInfo;

    private static final long serialVersionUID = 1L;
}