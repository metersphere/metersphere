package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class EnvironmentGroupProject implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{environment_group_project.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{environment_group_project.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "环境组id")
    private String environmentGroupId;

    @Schema(title = "api test environment 环境ID")
    private String environmentId;

    @Schema(title = "项目id")
    private String projectId;

    private static final long serialVersionUID = 1L;
}