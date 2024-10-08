package io.metersphere.system.dto.sdk;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectBaseRequest {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.organization_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{project.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{project.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "项目描述")
    @Size(min = 0, max = 1000, message = "{project.description.length_range}", groups = {Created.class, Updated.class})
    private String description;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "全部资源池")
    private boolean allResourcePool = false;

    @Schema(description = "模块设置", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> moduleIds;

    @Schema(description = "成员数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{project.member_count.not_blank}", groups = {Created.class, Updated.class})
    private List<String> userIds;

    @Schema(description = "资源池", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> resourcePoolIds;
}
