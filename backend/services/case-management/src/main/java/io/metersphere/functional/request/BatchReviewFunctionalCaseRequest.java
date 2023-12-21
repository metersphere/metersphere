package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BatchReviewFunctionalCaseRequest extends BaseReviewCaseBatchRequest{

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

}
