package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class EnvironmentGroup implements Serializable {
    @Schema(title = "环境组id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{environment_group.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "环境组名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{environment_group.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "所属工作空间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group.workspace_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment_group.workspace_id.length_range}", groups = {Created.class, Updated.class})
    private String workspaceId;

    @Schema(title = "环境组描述")
    private String description;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment_group.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}