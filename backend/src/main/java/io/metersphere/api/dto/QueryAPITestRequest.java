package io.metersphere.api.dto;

import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryAPITestRequest {

    private String id;
    private String excludeId;
    private String projectId;
    private String name;
    private String workspaceId;
    private boolean recent = false;
    private List<OrderRequest> orders;
    private Map<String, List<String>> filters;
    private Map<String, Object> combine;
    private List<String> ids;
}
