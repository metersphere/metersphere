package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FunctionalCasePlanMindRequest extends FunctionalCaseMindRequest {

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_functional_case.test_plan_id.not_blank}")
    private String planId;


}
