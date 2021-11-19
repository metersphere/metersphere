package io.metersphere.api.dto;

import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiCaseRunRequest {
    private String reportId;
    private String id;
    private List<String> ids;
    private List<OrderRequest> orders;
    private String projectId;
    private String environmentId;
    private ApiTestCaseRequest condition;
}
