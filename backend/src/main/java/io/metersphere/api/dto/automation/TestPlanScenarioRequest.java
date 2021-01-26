package io.metersphere.api.dto.automation;

import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestPlanScenarioRequest {
    private String id;
    private String excludeId;
    private String projectId;
    private String moduleId;
    private List<String> moduleIds;
    private String name;
    private String status;
    private String workspaceId;
    private String userId;
    private String planId;
    private boolean recent = false;
    private List<OrderRequest> orders;
    private Map<String, List<String>> filters;
    private Map<String, Object> combine;
    private List<String> ids;
}
