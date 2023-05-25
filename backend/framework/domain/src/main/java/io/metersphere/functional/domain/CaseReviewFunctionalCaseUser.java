package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "功能用例评审和评审人的中间表")
@Table("case_review_functional_case_user")
@Data
public class CaseReviewFunctionalCaseUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case_user.case_id.not_blank}", groups = {Created.class})
    @Schema(title = "功能用例和评审中间表的ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String caseId;

    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.review_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case_user.review_id.not_blank}", groups = {Created.class})
    @Schema(title = "评审ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String reviewId;

    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.user_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case_user.user_id.not_blank}", groups = {Created.class})
    @Schema(title = "评审人ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String userId;


}