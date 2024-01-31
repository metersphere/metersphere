package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class SwaggerUrlCheck {
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug.project_id.length_range}")
    private String projectId;
    @Schema(description = "swagger地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_swagger.swagger_url.not_blank}")
    @Size(min = 1, max = 500, message = "{api_definition_swagger.swagger_url.length_range}")
    private String swaggerUrl;
}
