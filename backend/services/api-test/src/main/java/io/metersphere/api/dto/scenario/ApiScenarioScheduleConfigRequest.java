package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ApiScenarioScheduleConfigRequest {

    @NotBlank(message = "{api_scenario.id.not_blank}")
    @Schema(description = "场景ID")
    private String scenarioId;

    @Schema(description = "启用/禁用")
    private boolean enable;

    @Schema(description = "Cron表达式")
    @NotBlank
    private String cron;

    @Schema(description = "定时任务配置 (如果配置不更改，不需要传入这个参数。 如果要清空配置，传入一个空数据{} )")
    Map<String, Object> configMap;

}
