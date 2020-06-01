package io.metersphere.track.request.testplan;

import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryTestPlanRequest extends TestPlanRequest {
    private String workspaceId;
    private List<OrderRequest> orders;
}
