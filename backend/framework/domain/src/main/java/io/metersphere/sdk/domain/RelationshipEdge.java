package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class RelationshipEdge implements Serializable {
    @Schema(description =  "源节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{relationship_edge.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{relationship_edge.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(description =  "目标节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{relationship_edge.target_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{relationship_edge.target_id.length_range}", groups = {Created.class, Updated.class})
    private String targetId;

    @Schema(description =  "边的分类", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{relationship_edge.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{relationship_edge.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description =  "所属关系图的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{relationship_edge.graph_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{relationship_edge.graph_id.length_range}", groups = {Created.class, Updated.class})
    private String graphId;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "")
    private Long createTime;

    private static final long serialVersionUID = 1L;
}