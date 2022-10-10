package io.metersphere.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReportRequest extends BaseQueryRequest {
    private String name;
    private String workspaceId;
    private String testId;
    private String userId;
    private List<OrderRequest> orders;
    private Map<String, List<String>> filters;
    private Map<String, Object> combine;
    private String projectId;
}
