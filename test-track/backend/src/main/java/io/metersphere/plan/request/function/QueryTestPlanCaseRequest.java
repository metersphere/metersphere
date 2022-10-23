package io.metersphere.plan.request.function;

import io.metersphere.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryTestPlanCaseRequest extends BaseQueryRequest {

    private List<String> nodePaths;

    private List<String> planIds;

    private List<String> projectIds;

    private String workspaceId;

    private String status;

    private String node;

    private String method;

    private String nodeId;

    private String planId;

    private String executor;

    private String id;

    private String projectName;

    private Map<String, Object> combine;
}
