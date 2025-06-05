package io.metersphere.api.dto.definition;

import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiTestCaseAIRequest extends AIChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;


    @Schema(description = "接口定义ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String apiDefinitionId;

    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configId;
}
