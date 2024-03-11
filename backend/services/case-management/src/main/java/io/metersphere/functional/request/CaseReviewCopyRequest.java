package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaseReviewCopyRequest extends CaseReviewRequest{

    @Schema(description = "被复制的用例评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.copy_id.not_blank}")
    private String copyId;
}
