package io.metersphere.track.dto;

import io.metersphere.base.domain.IssuesDao;
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
    private List<IssuesDao> Issues;
    private List<String> executors;
    private List<String> executorNames;
    private String principal;
    private String principalName;
    private Long startTime;
    private Long endTime;
    private String projectName;

}
