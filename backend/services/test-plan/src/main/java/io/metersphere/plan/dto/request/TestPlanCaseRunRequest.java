package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanCaseRunRequest {

    @Schema(description = "项目Id")
    @NotBlank(message = "{test_plan.project_id.not_blank}")
    private String projectId;

    @Schema(description = "id")
    @NotBlank(message = "{id.not_blank}")
    private String id;

    @Schema(description = "测试计划id")
    @NotBlank(message = "{test_plan_id.not_blank}")
    private String testPlanId;

    @Schema(description = "用例id")
    @NotBlank(message = "{case_id.not_blank}")
    private String caseId;

    @Schema(description = "最终执行结果")
    @NotBlank(message = "{test_plan.last_exec_result.not_blank}")
    private String lastExecResult;

    @Schema(description = "步骤执行结果")
    private String stepsExecResult;

    @Schema(description = "执行内容")
    private String content;

    @Schema(description = "评论@的人的Id, 多个以';'隔开")
    private String notifier;

    @Schema(description = "测试计划执行评论富文本的文件id集合")
    private List<String> planCommentFileIds;

}
