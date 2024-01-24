package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
@Data
public class ApiScenarioDebugRequest {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.id.not_blank}")
    @Size(max = 50, message = "{api_scenario.id.length_range}")
    private String id;

    @Schema(description = "是否为环境组")
    private Boolean grouped = false;

    @Schema(description = "环境或者环境组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String environmentId;

    @Schema(description = "场景的通用配置")
    private ScenarioConfig scenarioConfig;

    @Valid
    @Schema(description = "步骤集合")
    private List<ApiScenarioStepRequest> steps;

    /**
     * 步骤详情
     * key 为步骤ID
     * 值  为详情
     */
    @Schema(description = "步骤详情")
    private Map<String, Object> stepDetails;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String projectId;
    @Schema(description = "点击调试时尚未保存的文件ID列表")
    private List<String> tempFileIds;
}
