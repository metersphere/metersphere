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

@Schema(title = "脑图临时节点")
@Table("minder_extra_node")
@Data
public class MinderExtraNode implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{minder_extra_node.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 50, message = "{minder_extra_node.parent_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{minder_extra_node.parent_id.not_blank}", groups = {Created.class})
    @Schema(title = "父节点的ID，即模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentId;

    @Size(min = 1, max = 50, message = "{minder_extra_node.group_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{minder_extra_node.group_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目ID，可扩展为其他资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String groupId;

    @Size(min = 1, max = 30, message = "{minder_extra_node.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{minder_extra_node.type.not_blank}", groups = {Created.class})
    @Schema(title = "类型，如：用例编辑脑图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;


    @Schema(title = "存储脑图节点额外信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nodeData;


}