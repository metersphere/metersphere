package io.metersphere.api.dto.scenario;

import io.metersphere.api.constants.ApiScenarioStatus;
import io.metersphere.system.valid.EnumValue;
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
public class ApiScenarioAddRequest {
    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.name.not_blank}")
    @Size(min = 1, max = 255, message = "{api_scenario.name.length_range}")
    private String name;

    @Schema(description = "场景级别/P0/P1等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.priority.not_blank}")
    @Size(min = 1, max = 10, message = "{api_scenario.priority.length_range}")
    private String priority;

    @Schema(description = "场景状态/未规划/已完成 等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.status.not_blank}")
    @Size(min = 1, max = 20, message = "{api_scenario.status.length_range}")
    @EnumValue(enumClass = ApiScenarioStatus.class)
    private String status;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_scenario.project_id.length_range}")
    private String projectId;

    @Schema(description = "场景模块fk")
    @NotBlank(message = "{api_debug.module_id.not_blank}")
    @Size(max = 50, message = "{api_scenario.module_id.length_range}")
    private String moduleId;

    @Schema(description = "描述信息")
    @Size(max = 1000, message = "{api_definition.description.length_range}")
    private String description;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "是否为环境组", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean grouped;

    @Schema(description = "环境或者环境组ID")
    @Size(max = 50, message = "{api_scenario.environment_id.length_range}")
    private String environmentId;

    @Schema(description = "场景的通用配置")
    private ScenarioConfig scenarioConfig = new ScenarioConfig();

    @Schema(description = "步骤集合")
    @Valid
    private List<ApiScenarioStepRequest> steps;

    /**
     * 步骤详情
     * key 为步骤ID
     * 值  为详情
     */
    @Schema(description = "步骤详情")
    private Map<String, Object> stepDetails;

    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;

    /**
     * 新关联的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds;
}
