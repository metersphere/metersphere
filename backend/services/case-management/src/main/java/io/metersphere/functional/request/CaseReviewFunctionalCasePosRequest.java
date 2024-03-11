package io.metersphere.functional.request;

import io.metersphere.system.dto.sdk.request.PosRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaseReviewFunctionalCasePosRequest extends PosRequest {

    @Schema(description = "用例评审Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.case_review_id.not_blank}")
    private String reviewId;
}
