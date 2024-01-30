package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestPlanModuleUpdateRequest {
    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{module.id.not_blank}")
    private String id;

    @Schema(description = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{module.name.not_blank}")
    @Size(min = 1, max = 255, message = "{test_plan_module.name.length_range}")
    private String name;
}

