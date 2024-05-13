package io.metersphere.plan.dto.request;

import io.metersphere.request.AssociateBugRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestPlanCaseAssociateBugRequest extends AssociateBugRequest {
    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String testPlanId;

    @Schema(description = "测试计划关联用例的id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String testPlanCaseId;


}
