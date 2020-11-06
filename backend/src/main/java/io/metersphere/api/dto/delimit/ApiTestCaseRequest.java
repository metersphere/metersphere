package io.metersphere.api.dto.delimit;

import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiTestCaseRequest {

    private String id;
    private String projectId;
    private String priority;
    private String name;
    private String environmentId;
    private String workspaceId;
    private String apiDelimitId;
    private List<OrderRequest> orders;
}
