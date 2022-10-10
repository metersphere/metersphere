package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RelationshipEdge extends RelationshipEdgeKey implements Serializable {
    private String type;

    private String graphId;

    private String creator;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}
