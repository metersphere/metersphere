package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanReportIssueDTO extends PlanReportCaseDTO {
    private String platformStatus;
    private String platform;
}
