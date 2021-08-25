package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestPlanWithBLOBs extends TestPlan implements Serializable {
    private String tags;

    private String reportSummary;

    private String reportConfig;

    private static final long serialVersionUID = 1L;
}