package io.metersphere.plan.request.api;


import io.metersphere.dto.TestPlanCaseReportResultDTO;
import io.metersphere.request.PlanSubReportRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ApiPlanReportRequest extends PlanSubReportRequest {
    List<String> apiReportIdList;
    List<String> scenarioReportIdList;
    private TestPlanCaseReportResultDTO testPlanExecuteReportDTO;
}
