package io.metersphere.sdk.dto;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectBaseRequest {

    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.organization_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{project.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description =  "项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{project.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "项目描述")
    private String description;

    @Schema(description =  "是否启用")
    private Boolean enable;

    @Schema(description =  "模块设置")
    private String moduleSetting;
}
