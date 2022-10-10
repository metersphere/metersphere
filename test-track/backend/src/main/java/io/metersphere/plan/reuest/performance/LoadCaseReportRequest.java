package io.metersphere.plan.reuest.performance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadCaseReportRequest {
    private String reportId;
    private String testPlanLoadCaseId;
}
