package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FunctionalCaseReviewMindRequest extends FunctionalCaseMindRequest {

    @Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.case_review_id.not_blank}")
    private String reviewId;

    @Schema(description = "是否只看我的", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean viewFlag;

    @Schema(description = "我的评审结果", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean viewStatusFlag;

}
