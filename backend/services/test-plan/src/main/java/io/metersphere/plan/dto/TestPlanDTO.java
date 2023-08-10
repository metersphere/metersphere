package io.metersphere.plan.dto;

import io.metersphere.plan.domain.TestPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TestPlanDTO extends TestPlan {
    @Schema(description =  "测试计划责任人", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> principals;

    @Schema(description =  "测试计划关注人")
    private List<String> followers;

    @Schema(description =  "是否自定更新功能用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean automaticStatusUpdate;

    @Schema(description =  "是否允许重复添加用例", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean repeatCase;

    @Schema(description =  "测试计划通过阈值;0-100", requiredMode = Schema.RequiredMode.REQUIRED)
    private int passThreshold = 100;
}
