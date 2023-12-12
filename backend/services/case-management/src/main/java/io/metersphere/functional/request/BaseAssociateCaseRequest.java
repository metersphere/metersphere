package io.metersphere.functional.request;

import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BaseAssociateCaseRequest extends BaseFunctionalCaseBatchDTO {

    @Schema(description = "功能用例选择的项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_associate_request.project_id.not_blank}")
    private String projectId;

}
