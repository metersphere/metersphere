package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
@Data
public class ApiScenarioStepRequest extends ApiScenarioStepCommonDTO {
    @Schema(description = "步骤名称")
    @NotBlank(message = "{api_scenario_step.name.not_blank}")
    @Size(min = 1, max = 255, message = "{api_scenario_step.name.length_range}")
    private String name;

    @Schema(description = "资源编号")
    @Size(min = 1, max = 50, message = "{api_scenario_step.resource_num.length_range}")
    private String resourceNum;

    @Schema(description = "项目fk")
    @Size(min = 1, max = 50, message = "{api_scenario_step.project_id.length_range}")
    private String projectId;

    @Schema(description = "版本号")
    @Size(min = 1, max = 50, message = "{api_scenario_step.version_id.length_range}")
    private String versionId;
}
