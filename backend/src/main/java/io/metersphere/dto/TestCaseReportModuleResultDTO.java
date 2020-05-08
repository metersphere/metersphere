package io.metersphere.dto;

import lombok.Data;

@Data
public class TestCaseReportModuleResultDTO {
    private String moduleId;
    private String moduleName;
    private Integer caseCount;
    private Integer passCount;
    private Double passRate;
    private Integer flawCount;
}
