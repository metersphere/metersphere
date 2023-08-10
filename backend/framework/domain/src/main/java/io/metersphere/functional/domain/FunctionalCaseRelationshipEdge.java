package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseRelationshipEdge implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "源节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(description =  "目标节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.target_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.target_id.length_range}", groups = {Created.class, Updated.class})
    private String targetId;

    @Schema(description =  "所属关系图的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.graph_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.graph_id.length_range}", groups = {Created.class, Updated.class})
    private String graphId;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "创建时间")
    private Long createTime;

    private static final long serialVersionUID = 1L;
}