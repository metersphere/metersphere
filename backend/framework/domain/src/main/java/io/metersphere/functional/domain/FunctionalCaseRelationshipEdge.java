package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseRelationshipEdge implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "源节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(title = "目标节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.target_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.target_id.length_range}", groups = {Created.class, Updated.class})
    private String targetId;

    @Schema(title = "所属关系图的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.graph_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.graph_id.length_range}", groups = {Created.class, Updated.class})
    private String graphId;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建时间")
    private Long createTime;

    private static final long serialVersionUID = 1L;
}