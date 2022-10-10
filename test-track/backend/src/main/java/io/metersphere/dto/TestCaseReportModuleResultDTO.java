package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseReportModuleResultDTO {
    private String moduleId;
    private String moduleName;
    private Integer caseCount;
    private Integer passCount;
    private Double passRate;
    private Integer issuesCount;
    private Integer prepareCount;
    private Integer skipCount;
    private Integer failureCount;
    private Integer blockingCount;
    private Integer underwayCount;
    private String projectName;
}
