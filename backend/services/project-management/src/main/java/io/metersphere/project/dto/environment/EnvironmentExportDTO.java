package io.metersphere.project.dto.environment;

import io.metersphere.sdk.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnvironmentExportDTO extends TableBatchProcessDTO {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_application.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{project_parameters.project_id.length_range}")
    private String projectId;



}
