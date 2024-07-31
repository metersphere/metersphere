package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanCaseBatchRunRequest extends BasePlanCaseBatchRequest {

    @Schema(description = "项目Id")
    private String projectId;

    @Schema(description = "最终执行结果")
    @NotBlank(message = "{test_plan.last_exec_result.not_blank}")
    private String lastExecResult;

    @Schema(description = "执行内容")
    private String content;

    @Schema(description = "评论@的人的Id, 多个以';'隔开")
    private String notifier;

    @Schema(description = "测试计划执行评论富文本的文件id集合")
    private List<String> planCommentFileIds;


}
