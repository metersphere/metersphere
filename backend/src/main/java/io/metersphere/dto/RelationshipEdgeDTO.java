package io.metersphere.dto;

import io.metersphere.base.domain.RelationshipEdge;
import lombok.Data;

@Data
public class RelationshipEdgeDTO extends RelationshipEdge {
    private String targetName;
    private Integer targetNum;
    private String targetCustomNum;
    private String status;
    private String creator;
}
