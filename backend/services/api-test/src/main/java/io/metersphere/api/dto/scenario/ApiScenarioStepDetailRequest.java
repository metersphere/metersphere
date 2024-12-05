package io.metersphere.api.dto.scenario;

import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApiScenarioStepDetailRequest {
    @Schema(description = "步骤id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50, message = "{api_scenario_step.id.length_range}")
    private String id;

    /**
     * 记录是从哪个步骤复制来的
     * 如果没有传步骤详情
     * 保存时需要根据这个字段查询原步骤详情保存
     */
    @Schema(description = "复制的目标步骤ID")
    private String copyFromStepId;

    @Schema(description = "资源id")
    private String resourceId;

    /**
     * {@link ApiScenarioStepType}
     */
    @Schema(description = "步骤类型/API/CASE等")
    @NotBlank
    @EnumValue(enumClass = ApiScenarioStepType.class)
    private String stepType;

    /**
     * 引用模式：默认完全引用
     * - 完全引用：步骤状态不可调整
     * - 部分引用：步骤状态可调整
     *
     * @see ApiScenarioStepRefType
     */
    @Schema(description = "引用/复制/自定义")
    @EnumValue(enumClass = ApiScenarioStepRefType.class)
    @NotBlank
    private String refType;
}
