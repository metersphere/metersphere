package io.metersphere.plan.dto;

import io.metersphere.dto.PlanReportCaseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanReportIssueDTO extends PlanReportCaseDTO {
    private String platformStatus;
    private String platform;
}
