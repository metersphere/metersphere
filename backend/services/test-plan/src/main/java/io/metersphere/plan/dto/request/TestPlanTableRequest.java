package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanTableRequest extends BasePageRequest {
    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<String> moduleIds;

    @Schema(description = "项目ID")
    @NotBlank(message = "{test_plan.project_id.not_blank}")
    private String projectId;

    @Schema(description = "类型", allowableValues = {"ALL", "TEST_PLAN", "GROUP"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.type.not_blank}")
    private String type;

    @Schema(description = "通过Keyword过滤出的测试子计划的测试计划组id")
    private List<String> keywordFilterIds;

    @Schema(description = "通过其他条件查询出来的，必须要包含的测试计划ID")
    private List<String> innerIds;
    private List<String> combineInnerIds;
    private String combineOperator;

    @Schema(description = "是否我的待办, 默认查询全部")
    private boolean myTodo = false;

    @Schema(description = "我的待办用户ID, 组合使用: myTodo=true, myTodoUserId=xxx")
    private String myTodoUserId;

    @Schema(description = "已办的测试计划ID集合 (用作待办排除)")
    private List<String> doneExcludeIds;

    @Schema(description = "额外的子计划ID集合")
    private List<String> extraIncludeChildIds;


    @Schema(description = "应当包含的子测试计划ID （用于程序内部筛选过滤）")
    private List<String> includeItemTestPlanIds;

    public String getSortString() {
        if (StringUtils.isEmpty(super.getSortString())) {
            return "t.update_time desc";
        } else {
            return "t." + super.getSortString();
        }

    }
}
