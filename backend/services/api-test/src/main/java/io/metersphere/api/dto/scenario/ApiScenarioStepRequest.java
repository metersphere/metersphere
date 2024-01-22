package io.metersphere.api.dto.scenario;

import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.system.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class ApiScenarioStepRequest {
    @Schema(description = "步骤id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_step.id.not_blank}")
    @Size(max = 50, message = "{api_scenario_step.id.length_range}")
    private String id;

    @Schema(description = "步骤名称")
    private String name;

    @Schema(description = "启用/禁用")
    private Boolean enable = true;

    @Schema(description = "资源id")
    private String resourceId;

    @Schema(description = "资源编号")
    private String resourceNum;

    /**
     * @see ApiScenarioStepType
     */
    @Schema(description = "步骤类型/API/CASE等")
    @NotBlank
    @EnumValue(enumClass = ApiScenarioStepType.class)
    private String stepType;

    @Schema(description = "项目fk")
    private String projectId;

    @Schema(description = "版本号")
    private String versionId;

    /**
     * 引用模式：默认完全引用
     *   - 完全引用：步骤状态不可调整
     *   - 部分引用：步骤状态可调整
     * @see io.metersphere.api.constants.ApiScenarioStepRefType
     */
    @Schema(description = "引用/复制/自定义")
    private String refType;

    @Schema(description = "循环等组件基础数据")
    private Map<Object, Object> config;

    @Schema(description = "子步骤")
    private List<ApiScenarioStepRequest> children;
}
