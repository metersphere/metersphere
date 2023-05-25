package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "测试计划关联UI场景")
@Table("test_plan_ui_scenario")
@Data
public class TestPlanUiScenario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_ui_scenario.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_ui_scenario.test_plan_id.not_blank}", groups = {Created.class})
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.ui_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_ui_scenario.ui_scenario_id.not_blank}", groups = {Created.class})
    @Schema(title = "UI场景ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String uiScenarioId;

    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_ui_scenario.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;


    @Schema(title = "排序，默认值5000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;


    @Schema(title = "环境类型", allowableValues = "range[1, 20]")
    private String environmentType;


    @Schema(title = "所属环境")
    private String environment;


    @Schema(title = "环境组ID", allowableValues = "range[1, 50]")
    private String environmentGroupId;


}