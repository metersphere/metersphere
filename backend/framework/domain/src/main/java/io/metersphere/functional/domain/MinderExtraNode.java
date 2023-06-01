package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class MinderExtraNode implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{minder_extra_node.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{minder_extra_node.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "父节点的ID，即模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{minder_extra_node.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{minder_extra_node.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(title = "项目ID，可扩展为其他资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{minder_extra_node.group_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{minder_extra_node.group_id.length_range}", groups = {Created.class, Updated.class})
    private String groupId;

    @Schema(title = "类型，如：用例编辑脑图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{minder_extra_node.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{minder_extra_node.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "存储脑图节点额外信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{minder_extra_node.node_data.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 2147483647, message = "{minder_extra_node.node_data.length_range}", groups = {Created.class, Updated.class})
    private String nodeData;

    private static final long serialVersionUID = 1L;
}