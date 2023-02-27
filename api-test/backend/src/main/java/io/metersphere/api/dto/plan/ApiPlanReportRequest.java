package io.metersphere.api.dto.plan;


import io.metersphere.request.PlanSubReportRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ApiPlanReportRequest extends PlanSubReportRequest {
    List<String> apiReportIdList;
    List<String> scenarioReportIdList;
    private TestPlanExecuteReportDTO testPlanExecuteReportDTO;
}
