package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SwaggerUrlCheck {
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;
    @Schema(description = "swagger地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String swaggerUrl;
}
