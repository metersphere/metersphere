package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class RelationshipEdgeKey implements Serializable {
    private String sourceId;

    private String targetId;

    private static final long serialVersionUID = 1L;
}
