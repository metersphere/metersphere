package io.metersphere.project.dto.environment;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class EnvironmentGroupRequest {

    @Schema(description = "环境组id")
    private String id;
    @Schema(description = "环境组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String projectId;
    @Schema(description = "环境组描述")
    private String description;
    @Schema(description = "环境组id")
    private List<EnvironmentGroupProjectDTO> envGroupProject;
}
