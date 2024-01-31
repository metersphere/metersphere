package io.metersphere.api.dto.scenario;

import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApiScenarioScheduleConfigRequest {

    @NotBlank(message = "{api_scenario.id.not_blank}")
    @Schema(description = "场景ID")
    @Size(min = 1, max = 50, message = "{api_scenario.id.length_range}")
    private String scenarioId;

    @Schema(description = "启用/禁用")
    private boolean enable;

    @Schema(description = "Cron表达式")
    @NotBlank
    private String cron;

    @Schema(description = "定时任务配置")
    private ApiRunModeConfigDTO config;

}
