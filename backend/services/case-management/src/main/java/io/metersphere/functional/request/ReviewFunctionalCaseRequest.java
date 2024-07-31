package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ReviewFunctionalCaseRequest {

    @Schema(description = "项目Id")
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "用例评审Id")
    @NotBlank(message = "{case_review.case_review_id.not_blank}")
    private String reviewId;

    @Schema(description = "功能用例id")
    @NotBlank(message = "{functional_case_blob.functional_case_id.not_blank}")
    private String caseId;

    @Schema(description = "评审规则")
    @NotBlank(message = "{case_review.review_pass_rule.not_blank}")
    private String reviewPassRule;

    @Schema(description = "评审结果")
    @NotBlank(message = "{functional_case.status.not_blank}")
    private String status;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description =  "评论@的人的Id, 多个以';'隔开")
    private String  notifier;

    @Schema(description = "用例评审评论富文本的文件id集合")
    private List<String> reviewCommentFileIds;
}
