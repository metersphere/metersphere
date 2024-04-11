package io.metersphere.api.dto.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScenarioStepConfig extends ApiScenario {
    @Schema(description = "是否使用源场景环境")
    private Boolean enableScenarioEnv = false;

    /**
     * 是否使用源场景参数
     * 如果为 true，则当前场景和源场景的参数都应用
     * 如果为 false，则使用当前场景参数
     */
    @Schema(description = "是否使用源场景参数")
    private Boolean useOriginScenarioParam = false;

    /**
     * 当 useOriginScenarioParam 为 true 时
     * 是否优先使用源场景参数
     * 如果为 true，则和当前参数冲突时，优先使用源场景参数
     * 如果为 false，则和当前参数冲突时，优先使用当前场景参数
     */
    @Schema(description = "是否优先使用源场景参数")
    private Boolean useOriginScenarioParamPreferential = true;
}
