package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BatchReviewFunctionalCaseRequest extends BaseReviewCaseBatchRequest{

    @Schema(description = "评审规则:单人评审(SINGLE)/多人评审(MULTIPLE)")
    @NotBlank(message = "{case_review.review_pass_rule.not_blank}")
    private String reviewPassRule;

    @Schema(description = "评审结果:未评审(UN_REVIEWED)/评审中(UNDER_REVIEWED)/PASS(通过)/UN_PASS(未通过)/RE_REVIEWED(重新提审)")
    @NotBlank(message = "{functional_case.status.not_blank}")
    private String status;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description =  "评论@的人的Id, 多个以';'隔开")
    private String  notifier;

}
