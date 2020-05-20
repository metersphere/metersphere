package io.metersphere.track.dto;

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
}
