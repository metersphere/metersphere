package io.metersphere.track.dto;

import io.metersphere.base.domain.Issues;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
}
