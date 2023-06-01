package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class CaseReviewFunctionalCaseUser implements Serializable {
    @Schema(title = "功能用例和评审中间表的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_functional_case_user.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(title = "评审ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_functional_case_user.review_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.review_id.length_range}", groups = {Created.class, Updated.class})
    private String reviewId;

    @Schema(title = "评审人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_functional_case_user.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    private static final long serialVersionUID = 1L;
}