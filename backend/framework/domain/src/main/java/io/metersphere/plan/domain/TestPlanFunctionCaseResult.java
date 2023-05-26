package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanFunctionCaseResult implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_function_case_result.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_function_case_result.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "测试计划功能用例关联表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_function_case_result.test_plan_function_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_function_case_result.test_plan_function_case_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanFunctionCaseId;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_function_case_result.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_function_case_result.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "执行结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_function_case_result.result.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_function_case_result.result.length_range}", groups = {Created.class, Updated.class})
    private String result;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "评论")
    private String comment;

    @Schema(title = "所属版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_function_case_result.version.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_function_case_result.version.length_range}", groups = {Created.class, Updated.class})
    private String version;

    private static final long serialVersionUID = 1L;
}