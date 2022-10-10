package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryReferenceRequest {
    private String scenarioId;
    private String apiId;
    private String loadId;
    private String projectId;
}
