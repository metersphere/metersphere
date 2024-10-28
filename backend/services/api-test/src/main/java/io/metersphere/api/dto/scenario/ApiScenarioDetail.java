package io.metersphere.api.dto.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.system.uid.IDGenerator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioDetail extends ApiScenario {
    @Schema(description = "模块路径")
    private String modulePath;
    @Schema(description = "场景的通用配置")
    private ScenarioConfig scenarioConfig = new ScenarioConfig();
    @Schema(description = "步骤")
    private List<ApiScenarioStepDTO> steps;

    public void resetConfigCsvId() {
        if (scenarioConfig == null || scenarioConfig.getVariable() == null || scenarioConfig.getVariable().getCsvVariables() == null) {
            return;
        }

        scenarioConfig.getVariable().getCsvVariables().forEach(csvVariable -> {
            csvVariable.setId(IDGenerator.nextStr());
        });
    }
}
