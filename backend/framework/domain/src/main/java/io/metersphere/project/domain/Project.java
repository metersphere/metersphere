package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class Project implements Serializable {
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "工作空间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.workspace_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{project.workspace_id.length_range}", groups = {Created.class, Updated.class})
    private String workspaceId;

    @Schema(title = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{project.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "项目描述")
    private String description;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "")
    private String systemId;

    private static final long serialVersionUID = 1L;
}