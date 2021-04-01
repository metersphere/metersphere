package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestPlan;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryTestPlanRequest extends TestPlan {

    private boolean recent = false;

    private List<String> planIds;

    private String scenarioId;

    private String apiId;

    private String loadId;

    private List<OrderRequest> orders;

    private Map<String, List<String>> filters;

    private Map<String, Object> combine;

    private String projectId;

    private String projectName;
}
