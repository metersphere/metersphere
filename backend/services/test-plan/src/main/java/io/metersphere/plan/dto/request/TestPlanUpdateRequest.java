package io.metersphere.plan.dto.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.LinkedHashSet;

@Data
public class TestPlanUpdateRequest {
    @Schema(description = "测试计划ID")
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String id;

    @Schema(description = "测试计划名称")
    @Size(min = 1, max = 255, message = "{test_plan.name.length_range}")
    private String name;

    @Schema(description = "模块ID")
    private String moduleId = ModuleConstants.DEFAULT_NODE_ID;

    @Schema(description = "标签")
    private LinkedHashSet<String> tags;

    @Schema(description = "计划开始时间")
    private Long plannedStartTime;

    @Schema(description = "计划结束时间")
    private Long plannedEndTime;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否开启测试规划")
    private Boolean testPlanning;

    @Schema(description = "是否自定更新功能用例状态")
    private Boolean automaticStatusUpdate;

    @Schema(description = "是否允许重复添加用例")
    private Boolean repeatCase;

    @Schema(description = "测试计划通过阈值;0-100", requiredMode = Schema.RequiredMode.REQUIRED)
    @Max(value = 100)
    @Min(value = 0)
    private Double passThreshold;

    @Schema(description = "测试计划组Id")
    private String groupId = TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID;
}
