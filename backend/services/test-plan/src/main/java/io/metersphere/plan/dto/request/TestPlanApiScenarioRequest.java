package io.metersphere.plan.dto.request;

import io.metersphere.api.dto.scenario.ApiScenarioPageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class TestPlanApiScenarioRequest extends ApiScenarioPageRequest {

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;

    @Schema(description = "计划集id")
    private String collectionId;

}