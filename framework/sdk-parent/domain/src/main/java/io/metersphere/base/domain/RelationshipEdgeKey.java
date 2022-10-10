package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class RelationshipEdgeKey implements Serializable {
    private String sourceId;

    private String targetId;

    private static final long serialVersionUID = 1L;
}
