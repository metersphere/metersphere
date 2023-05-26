package io.metersphere.ui.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiCustomCommandModule implements Serializable {
    @Schema(title = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command_module.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{ui_custom_command_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "父级ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command_module.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(title = "模块等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command_module.level.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{ui_custom_command_module.level.length_range}", groups = {Created.class, Updated.class})
    private Integer level;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command_module.pos.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 22, message = "{ui_custom_command_module.pos.length_range}", groups = {Created.class, Updated.class})
    private Double pos;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command_module.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command_module.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    private static final long serialVersionUID = 1L;
}