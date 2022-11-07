package io.metersphere.api.dto;

import io.metersphere.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryReferenceRequest extends BaseQueryRequest {
    private String scenarioId;
    private String apiId;
    private String loadId;
    private String projectId;
    private String workspaceId;
    private String id;
}
