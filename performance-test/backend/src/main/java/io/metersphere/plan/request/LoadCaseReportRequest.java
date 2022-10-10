package io.metersphere.plan.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadCaseReportRequest {
    private String reportId;
    private String testPlanLoadCaseId;
}
