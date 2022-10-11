package io.metersphere.dto;

import io.metersphere.xpack.track.dto.IssuesDao;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Deprecated
public class TestCaseReportMetricDTO {

    private TestCaseReportAdvanceStatusResultDTO executeResult;
    private List<TestCaseReportModuleResultDTO> moduleExecuteResult;
    private io.metersphere.dto.FailureTestCasesAdvanceDTO failureTestCases;
    private List<IssuesDao> Issues;
    private List<String> executors;
    private List<String> executorNames;
    private String principal;
    private String principalName;
    private Long startTime;
    private Long endTime;
    private String projectName;

}
