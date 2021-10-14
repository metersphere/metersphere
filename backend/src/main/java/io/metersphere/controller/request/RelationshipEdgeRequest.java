package io.metersphere.controller.request;

import io.metersphere.base.domain.RelationshipEdge;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RelationshipEdgeRequest extends RelationshipEdge {
    private String id;
    private List<String> targetIds;
    private List<String> sourceIds;
}
