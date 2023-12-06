package io.metersphere.project.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import java.io.Serializable;

@Data
public class EnvironmentGroupProjectDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "环境组id")
    private String environmentGroupId;
    @Schema(description = "环境id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String environmentId;
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String projectId;
}