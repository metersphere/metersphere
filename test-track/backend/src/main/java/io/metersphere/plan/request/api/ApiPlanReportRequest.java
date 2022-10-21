package io.metersphere.plan.request.api;


import io.metersphere.dto.TestPlanExecuteReportDTO;
import io.metersphere.request.PlanSubReportRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiPlanReportRequest extends PlanSubReportRequest {
    private TestPlanExecuteReportDTO testPlanExecuteReportDTO;
}
