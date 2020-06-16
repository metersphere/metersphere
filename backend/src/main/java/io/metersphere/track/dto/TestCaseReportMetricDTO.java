package io.metersphere.track.dto;

import io.metersphere.track.domain.ReportBaseInfoComponent;
import io.metersphere.track.domain.ReportFailureResultComponent;
import io.metersphere.track.domain.ReportResultChartComponent;
import io.metersphere.track.domain.ReportResultComponent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseReportMetricDTO {

    private List<TestCaseReportStatusResultDTO> executeResult;
    private List<TestCaseReportModuleResultDTO> moduleExecuteResult;
    private List<TestPlanCaseDTO> failureTestCases;
    private List<String> executors;
    private String principal;
    private Long startTime;
    private Long endTime;
    private String projectName;

}
