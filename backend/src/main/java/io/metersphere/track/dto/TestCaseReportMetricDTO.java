package io.metersphere.track.dto;

import io.metersphere.base.domain.Issues;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseReportMetricDTO {

//    private List<TestCaseReportStatusResultDTO> executeResult;
    private TestCaseReportAdvanceStatusResultDTO executeResult;
    private List<TestCaseReportModuleResultDTO> moduleExecuteResult;
    private FailureTestCasesAdvanceDTO failureTestCases;
//    private List<TestPlanCaseDTO> failureTestCases;
    private List<Issues> Issues;
    private List<String> executors;
    private String principal;
    private Long startTime;
    private Long endTime;
    private String projectName;

}
