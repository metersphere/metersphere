package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseReportMetricDTO {

    private List<TestCaseReportStatusResultDTO> executeResult;
    private List<TestCaseReportModuleResultDTO> moduleExecuteResult;
    private List<String> executors;
    private String principal;
    private Long startTime;
    private Long endTime;
    private String projectName;

}
