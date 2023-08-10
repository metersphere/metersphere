package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalMinderExtraNode implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_minder_extra_node.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_minder_extra_node.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "父节点的ID，即模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_minder_extra_node.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_minder_extra_node.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(description =  "项目ID，可扩展为其他资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_minder_extra_node.group_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_minder_extra_node.group_id.length_range}", groups = {Created.class, Updated.class})
    private String groupId;

    @Schema(description =  "存储脑图节点额外信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_minder_extra_node.node_data.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 2147483647, message = "{functional_minder_extra_node.node_data.length_range}", groups = {Created.class, Updated.class})
    private String nodeData;

    private static final long serialVersionUID = 1L;
}