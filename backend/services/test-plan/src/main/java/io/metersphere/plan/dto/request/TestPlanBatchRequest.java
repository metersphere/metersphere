package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class TestPlanBatchRequest extends TestPlanBatchProcessRequest {

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.module_id.not_blank}")
    private String moduleId;
}
