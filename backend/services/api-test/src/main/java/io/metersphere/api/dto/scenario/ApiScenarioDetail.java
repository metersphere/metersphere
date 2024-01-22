package io.metersphere.api.dto.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioDetail extends ApiScenario {
    @Schema(description = "场景的通用配置")
    private ScenarioConfig scenarioConfig;
}
