package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanUiScenario implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_ui_scenario.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_ui_scenario.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(title = "UI场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_ui_scenario.ui_scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.ui_scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String uiScenarioId;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "排序，默认值5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_ui_scenario.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(title = "环境类型")
    private String environmentType;

    @Schema(title = "环境组ID")
    private String environmentGroupId;

    @Schema(title = "所属环境")
    private String environment;

    private static final long serialVersionUID = 1L;
}