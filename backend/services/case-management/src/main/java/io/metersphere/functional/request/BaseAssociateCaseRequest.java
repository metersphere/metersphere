package io.metersphere.functional.request;

import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BaseAssociateCaseRequest extends BaseFunctionalCaseBatchDTO {

    @Schema(description = "功能用例选择的项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "默认评审人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{case_review.reviewers.not_empty}")
    private List<String> reviewers;

}
