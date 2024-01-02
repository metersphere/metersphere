package io.metersphere.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * @author wx
 */
@Data
public class RelationshipEdgeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sourceId;
    private String targetId;
    private String graphId;


    public RelationshipEdgeDTO(String sourceId, String targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }
}