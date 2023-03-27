package io.metersphere.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TestPlanLoadCaseStatusDTO {
    private String planCaseId;
    private String planCaseStatus;
    private String planCaseReportId;
    private String planCaseReportStatus;
}
