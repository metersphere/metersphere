package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.debug.ApiDebugRunRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ApiCaseRunRequest extends ApiDebugRunRequest {
    @Schema(description = "环境ID")
    private String environmentId;
    @Schema(description = "接口定义ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String apiDefinitionId;
}
