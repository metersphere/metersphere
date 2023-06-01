package io.metersphere.plan.dto;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanDTO extends TestPlan {
    @Schema(title = "测试计划责任人", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @NotEmpty(message = "{test_plan_principal.user_id.not_blank}", groups = {Created.class})
    private List<String> principals;

    @Schema(title = "测试计划关注人")
    private List<String> followers;
}
