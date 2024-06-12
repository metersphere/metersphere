package io.metersphere.plan.dto.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Data
public class TestPlanCreateRequest {
    @Schema(description = "测试计划所属项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{test_plan.project_id.length_range}")
    private String projectId;

    @Schema(description = "测试计划组ID;测试计划要改为树结构。最上层的为NONE，其余则是父节点ID")
    @Size(min = 1, max = 50, message = "{test_plan.parent_id.length_range}")
    private String groupId = TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID;

    @Schema(description = "测试计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.name.not_blank}")
    @Size(min = 1, max = 255, message = "{test_plan.name.length_range}")
    private String name;


    @Schema(description = "测试计划模块ID")
    @Size(min = 1, max = 50, message = "{test_plan.parent_id.length_range}")
    private String moduleId = ModuleConstants.DEFAULT_NODE_ID;

    @Schema(description = "计划开始时间")
    private Long plannedStartTime;

    @Schema(description = "计划结束时间")
    private Long plannedEndTime;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
                    String> tags;

    @Schema(description = "描述")
    private String description;

    @Schema(description =  "是否自定更新功能用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean automaticStatusUpdate;

    @Schema(description =  "是否允许重复添加用例", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean repeatCase;

    @Schema(description =  "测试计划通过阈值;0-100", requiredMode = Schema.RequiredMode.REQUIRED)
    @Max(value = 100, message = "{test_plan.pass_threshold.max}")
    @Min(value = 0)
    private double passThreshold = 100;
    @Schema(description = "测试计划类型",allowableValues ={"TEST_PLAN", "GROUP"}, requiredMode = Schema.RequiredMode.REQUIRED )
    private String type = TestPlanConstants.TEST_PLAN_TYPE_PLAN;

    public List<String> getTags() {
        return tags == null ? null : new ArrayList<>(tags);
    }

    public boolean isGroupOption() {
        return StringUtils.equals(this.type, TestPlanConstants.TEST_PLAN_TYPE_GROUP) || !StringUtils.equals(this.groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
    }

    @Schema(description = "查询用例的条件")
    private BaseAssociateCaseRequest baseAssociateCaseRequest;
}
