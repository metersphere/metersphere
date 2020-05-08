package io.metersphere.dto;

import lombok.Data;

@Data
public class TestCaseReportModuleResultDTO {
    private String module;
    private Integer caseCount;
    private Integer passRate;
    private Integer flawCount;
}
