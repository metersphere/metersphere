package io.metersphere.api.dto.definition;

import io.metersphere.controller.request.RelationshipEdgeRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiDefinitionRelationshipEdgeRequest extends RelationshipEdgeRequest {
    private List<String> ids;
    private ApiDefinitionRequest condition;
}
