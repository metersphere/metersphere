package io.metersphere.functional.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FunctionalThirdDemandPageRequest extends BasePageRequest {


    @Schema(description = "ms系统当前的项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

}
