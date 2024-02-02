package io.metersphere.api.dto.scenario;

import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.system.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class StepRequest {
    @Schema(description = "步骤id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "{api_scenario_step.id.length_range}")
    @NotBlank
    private String stepId;
    @Schema(description = "资源id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "{api_scenario_step.resource_id.length_range}")
    @NotBlank
    private String resourceId;
    @Schema(description = "步骤类型  API:接口  API_CASE:用例  API_SCENARIO:场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @EnumValue(enumClass = ApiScenarioStepType.class)
    private String stepType;
}

