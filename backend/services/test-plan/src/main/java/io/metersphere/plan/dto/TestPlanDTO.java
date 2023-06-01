package io.metersphere.plan.dto;

import io.metersphere.plan.domain.TestPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanDTO extends TestPlan {
    @Schema(title = "测试计划责任人", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> principals;

    @Schema(title = "测试计划关注人")
    private List<String> followers;
}
