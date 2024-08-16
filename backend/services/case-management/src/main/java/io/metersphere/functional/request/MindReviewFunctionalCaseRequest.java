package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MindReviewFunctionalCaseRequest{

    @Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_user.review_id.not_blank}")
    private String reviewId;

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_user.review_id.not_blank}")
    private String caseId;

    @Schema(description = "评审结果:未评审(UN_REVIEWED)/评审中(UNDER_REVIEWED)/PASS(通过)/UN_PASS(未通过)/RE_REVIEWED(重新提审)")
    @NotBlank(message = "{case_review.status.not_blank}")
    private String status;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description =  "评论@的人的Id, 多个以';'隔开")
    private String  notifier;

    @Schema(description = "用例评审评论富文本的文件id集合")
    private List<String> reviewCommentFileIds;

    @Schema(description = "userId用来判断是否只看我的")
    private String userId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

}
