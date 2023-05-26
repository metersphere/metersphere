package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class Workspace implements Serializable {
    @Schema(title = "工作空间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{workspace.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{workspace.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "工作空间名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{workspace.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{workspace.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{workspace.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{workspace.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    private static final long serialVersionUID = 1L;
}