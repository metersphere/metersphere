package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiScenarioReportWithBLOBs extends ApiScenarioReport implements Serializable {
    private String description;

    private String envConfig;

    private static final long serialVersionUID = 1L;
}