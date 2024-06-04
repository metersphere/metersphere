package io.metersphere.plan.dto.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class TestPlanBatchRequest extends TestPlanBatchProcessRequest {

    @Schema(description = "目标ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.target_id.not_blank}")
    private String targetId;

    @Schema(description = "移动类型 （MODULE / GROUP)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveType = ModuleConstants.NODE_TYPE_DEFAULT;
}
