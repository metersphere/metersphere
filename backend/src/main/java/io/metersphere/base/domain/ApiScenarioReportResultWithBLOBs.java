package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiScenarioReportResultWithBLOBs extends ApiScenarioReportResult implements Serializable {
    private String errorCode;

    private static final long serialVersionUID = 1L;
}