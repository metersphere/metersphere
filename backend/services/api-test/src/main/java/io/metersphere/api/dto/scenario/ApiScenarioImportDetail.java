package io.metersphere.api.dto.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioImportDetail extends ApiScenario {
    @Schema(description = "场景配置信息")
    private ScenarioConfig scenarioConfig;
    @Schema(description = "模块路径")
    private String modulePath;
    @Schema(description = "步骤详情")
    private Map<String, Object> stepDetails;
    @Schema(description = "步骤集合")
    private List<ApiScenarioStepRequest> steps;
}
