package io.metersphere.plan.request.ui;


import io.metersphere.request.PlanSubReportRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UiPlanReportRequest extends PlanSubReportRequest {
    private TestPlanUiExecuteReportDTO testPlanExecuteReportDTO;
}
