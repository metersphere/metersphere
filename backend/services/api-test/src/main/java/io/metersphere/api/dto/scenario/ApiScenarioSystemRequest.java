package io.metersphere.api.dto.scenario;

import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioSystemRequest {
    @Schema(description = "接口的参数")
    private ScenarioSystemRequest apiRequest;
    @Schema(description = "用例的参数")
    private ScenarioSystemRequest caseRequest;
    @Schema(description = "场景的参数")
    private ScenarioSystemRequest scenarioRequest;
    @Schema(description = "关联类型 COPY:复制  REF:引用")
    @EnumValue(enumClass = ApiScenarioStepRefType.class)
    @NotBlank
    private String refType;
}

