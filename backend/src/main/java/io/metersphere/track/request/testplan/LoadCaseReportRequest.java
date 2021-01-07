package io.metersphere.track.request.testplan;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadCaseReportRequest {
    private String reportId;
    private String testPlanLoadCaseId;
}
