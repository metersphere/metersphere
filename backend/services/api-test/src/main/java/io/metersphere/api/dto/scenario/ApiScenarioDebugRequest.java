package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.ResourceAddFileParam;
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
public class ApiScenarioDebugRequest extends ApiScenarioParseParam {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.id.not_blank}")
    @Size(max = 50, message = "{api_scenario.id.length_range}")
    private String id;

    @Schema(description = "报告ID，传了可以实时获取结果，不传则不支持实时获取")
    @Size(max = 50)
    private String reportId;

    @Valid
    @Schema(description = "步骤集合")
    private List<ApiScenarioStepRequest> steps;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String projectId;

    @Schema(description = "是否是本地执行")
    private Boolean frontendDebug = false;

    /**
     * 步骤文件操作相关参数
     * key 为步骤ID
     * 值为文件参数
     */
    @Schema(description = "步骤文件操作相关参数")
    private Map<String, ResourceAddFileParam> stepFileParam;

    /**
     * 步骤文件操作相关参数
     */
    @Schema(description = "场景文件操作相关参数")
    private ResourceAddFileParam fileParam;
}
