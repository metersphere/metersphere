package io.metersphere.plan.dto.request;

import io.metersphere.api.dto.definition.ApiTestCasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class TestPlanApiCaseRequest extends ApiTestCasePageRequest {

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;
}