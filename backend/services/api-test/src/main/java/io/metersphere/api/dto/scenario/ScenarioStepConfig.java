package io.metersphere.api.dto.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScenarioStepConfig extends ApiScenario {
    @Schema(description = "是否使用源场景环境")
    private Boolean enableScenarioEnv = false;

    @Schema(description = "是否使用当前场景参数，如果为false，则使用源场景参数")
    private Boolean useCurrentScenarioParam = false;

    /**
     * 是否当前场景和源场景都应用
     * 如果为 true，则当前场景和源场景的参数都应用
     * 如果为 false，则根据 useCurrentScenarioParam 参数使用当前或者源场景的参数
     */
    @Schema(description = "是否当前场景和源场景都应用")
    private Boolean useBothScenarioParam = false;
}
