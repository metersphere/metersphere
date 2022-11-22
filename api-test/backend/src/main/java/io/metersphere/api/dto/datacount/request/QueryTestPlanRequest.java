package io.metersphere.api.dto.datacount.request;

import io.metersphere.request.OrderRequest;
import io.metersphere.request.TestPlanRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryTestPlanRequest extends TestPlanRequest {
    private String workspaceId;
    private String userId;
    private List<OrderRequest> orders;
    private Map<String, List<String>> filters;
    private Map<String, Object> combine;
}
