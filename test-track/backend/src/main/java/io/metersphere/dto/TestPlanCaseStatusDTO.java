package io.metersphere.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestPlanCaseStatusDTO {
    private String planCaseId;
    private String planCaseStatus;
}
