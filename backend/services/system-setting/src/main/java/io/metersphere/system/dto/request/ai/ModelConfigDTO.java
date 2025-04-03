package io.metersphere.system.dto.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ModelConfigDTO {
    @Schema(description = "基础名称（deepseek-0.5)")
    private String baseName;

    @Schema(description = "模型key")
    private String apiKey;

    @Schema(description = "模型url")
    private String apiUrl;
}
