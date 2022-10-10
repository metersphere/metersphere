package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestPlanWithBLOBs extends TestPlan implements Serializable {
    private String tags;

    private String reportSummary;

    private String reportConfig;

    private String runModeConfig;

    private static final long serialVersionUID = 1L;
}