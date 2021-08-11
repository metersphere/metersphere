package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TestPlanSimpleReportDTO {
    private Long startTime;
    private Long endTime;
    private int caseCount;
    private int executeCount;
    private int passCount;
    private double executeRate;
    private double passRate;
    private String summary;
    private TestPlanFunctionResultReportDTO functionResult;
    private TestPlanApiResultReportDTO apiResult;
    private TestPlanLoadResultReportDTO loadResult;
}
