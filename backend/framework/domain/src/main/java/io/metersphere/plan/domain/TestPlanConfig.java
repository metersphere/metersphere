package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanConfig implements Serializable {
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_config.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_config.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(title = "是否自定更新功能用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_config.automatic_status_update.not_blank}", groups = {Created.class})
    private Boolean automaticStatusUpdate;

    @Schema(title = "是否允许重复添加用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_config.repeat_case.not_blank}", groups = {Created.class})
    private Boolean repeatCase;

    @Schema(title = "测试计划通过阈值;0-100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_config.pass_threshold.not_blank}", groups = {Created.class})
    private Integer passThreshold;

    @Schema(title = "运行模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_config.run_mode_config.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 65535, message = "{test_plan_config.run_mode_config.length_range}", groups = {Created.class, Updated.class})
    private String runModeConfig;

    private static final long serialVersionUID = 1L;
}