package io.metersphere.project.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EnvironmentGroupProjectDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "环境id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String environmentId;
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String projectId;
}