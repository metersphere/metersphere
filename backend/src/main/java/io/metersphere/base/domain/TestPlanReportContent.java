package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanReportContent implements Serializable {
    private String id;

    private String testPlanReportId;

    private Long startTime;

    private Long caseCount;

    private Long endTime;

    private Double executeRate;

    private Double passRate;

    private Boolean isThirdPartIssue;

    private static final long serialVersionUID = 1L;
}