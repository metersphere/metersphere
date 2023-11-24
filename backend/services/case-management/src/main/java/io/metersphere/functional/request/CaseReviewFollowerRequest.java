package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author guoyuqi
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CaseReviewFollowerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_id.not_blank}")
    private String userId;

    @Schema(description = "用例评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.id.not_blank}")
    private String caseReviewId;

}
