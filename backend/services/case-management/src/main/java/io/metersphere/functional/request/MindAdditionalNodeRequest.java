package io.metersphere.functional.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author guoyuqi
 */
@Data
public class MindAdditionalNodeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{mind_additional_node.id.not_blank}", groups = {Updated.class})
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{mind_additional_node.name.not_blank}", groups = {Created.class})
    private String name;

    @Schema(description = "父节点ID()", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{mind_additional_node.parent_id.not_blank}", groups = {Created.class})
    private String parentId;

    @Schema(description = "操作类型（新增(ADD)/更新(UPDATE)）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.relation.not_blank}")
    private String type;

    @Schema(description = "移动方式（节点移动或新增时需要）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveMode;

    @Schema(description = "移动目标（节点移动或新增时需要）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetId;

}
