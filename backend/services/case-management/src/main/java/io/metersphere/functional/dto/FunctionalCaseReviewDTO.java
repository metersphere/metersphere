package io.metersphere.functional.dto;

import io.metersphere.functional.domain.CaseReviewFunctionalCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FunctionalCaseReviewDTO extends CaseReviewFunctionalCase {

    @Schema(description = "评审Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_functional_case.review_name.not_blank}")
    private String reviewNum;

    @Schema(description = "评审名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_functional_case.review_name.not_blank}")
    private String reviewName;

    @Schema(description = "用例评审状态:未开始(PREPARED)/进行中(UNDERWAY)/已完成(COMPLETED)/已归档(ARCHIVED)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_functional_case.review_status.not_blank}")
    private String reviewStatus;

}
