package io.metersphere.performance.request;

import io.metersphere.track.request.testplancase.TestPlanFuncCaseConditions;
import lombok.Data;

import java.util.List;

@Data
public class DeletePerformanceRequest {
    private List<String> ids;
    private TestPlanFuncCaseConditions condition;
    private String projectId;
}
