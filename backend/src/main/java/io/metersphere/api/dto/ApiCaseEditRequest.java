package io.metersphere.api.dto;

import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiCaseEditRequest extends ApiTestCaseWithBLOBs {
    private List<String> ids;
    private List<OrderRequest> orders;
    private String projectId;
    private String environmentId;
    private ApiTestCaseRequest condition;
}
