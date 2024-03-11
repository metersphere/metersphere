package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class CaseReviewAssociateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.case_review_id.not_blank}")
    private String reviewId;

    @Schema(description = "用例评审所在的项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "评审人")
    @NotEmpty(message = "{case_review.user_ids.not_empty}")
    private List<String> reviewers;

    @Schema(description = "查询功能用例的条件")
    @NotNull(message = "{case_review_associate_request.base_associate_case_request.not_null}")
    private BaseAssociateCaseRequest baseAssociateCaseRequest;

}
