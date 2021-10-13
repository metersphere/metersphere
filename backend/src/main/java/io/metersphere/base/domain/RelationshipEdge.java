package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class RelationshipEdge implements Serializable {
    private String sourceId;

    private String targetId;

    private String relationshipType;

    private String resourceType;

    private String graphId;

    private String creator;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}