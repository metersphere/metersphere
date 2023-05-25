package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "功能用例的前后置关系")
@Table("functional_case_relationship_edge")
@Data
public class FunctionalCaseRelationshipEdge implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{functional_case_relationship_edge.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.source_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_relationship_edge.source_id.not_blank}", groups = {Created.class})
    @Schema(title = "源节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceId;

    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.target_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_relationship_edge.target_id.not_blank}", groups = {Created.class})
    @Schema(title = "目标节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetId;

    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.graph_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_relationship_edge.graph_id.not_blank}", groups = {Created.class})
    @Schema(title = "所属关系图的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String graphId;

    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_relationship_edge.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;


    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;


}