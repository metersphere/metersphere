package io.metersphere.api.dto.plan;


import io.metersphere.request.PlanSubReportRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiPlanReportRequest extends PlanSubReportRequest {
    private TestPlanExecuteReportDTO testPlanExecuteReportDTO;
}
