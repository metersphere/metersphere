package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class EnvironmentGroupProject implements Serializable {
    @Schema(description =  "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group_project.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment_group_project.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "环境组id")
    private String environmentGroupId;

    @Schema(description =  "环境ID")
    private String environmentId;

    @Schema(description =  "项目id")
    private String projectId;

    private static final long serialVersionUID = 1L;
}