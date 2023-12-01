package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    @NotBlank(message = "{case_review_associate_request.case_review_id.not_blank}")
    private String reviewId;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_associate_request.project_id.not_blank}")
    private String projectId;

    @Schema(description = "功能用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{case_review_associate_request.case_ids.not_empty}")
    private List<String> caseIds;

    @Schema(description = "评审人")
    @NotEmpty(message = "{case_review_associate_request.user_ids.not_empty}")
    private List<String> reviewers;

}
