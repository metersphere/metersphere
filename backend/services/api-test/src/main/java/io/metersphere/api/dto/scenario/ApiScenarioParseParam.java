package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-03-01  20:33
 */
@Data
public class ApiScenarioParseParam {
    @Schema(description = "是否为环境组")
    private Boolean grouped = false;

    @Schema(description = "环境或者环境组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50, message = "{api_scenario.environment_id.length_range}")
    private String environmentId;

    @Schema(description = "场景的通用配置")
    private ScenarioConfig scenarioConfig;

    /**
     * 步骤详情
     * key 为步骤ID
     * 值  为详情
     */
    @Schema(description = "步骤详情")
    private Map<String, Object> stepDetails;
}
