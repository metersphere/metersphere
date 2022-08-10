package io.metersphere.api.dto;

import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryAPIReportRequest {

    private String id;
    private String projectId;
    private String name;
    private String workspaceId;
    private String userId;
    private String reportType;
    private boolean recent;
    private Boolean isUi = false;
    private List<OrderRequest> orders;
    private Map<String, List<String>> filters;
    private Map<String, Object> combine;

    /**
     * 查询哪种用例的报告 SCENARIO/API
     */
    private String caseType;

    private String limit;
}
