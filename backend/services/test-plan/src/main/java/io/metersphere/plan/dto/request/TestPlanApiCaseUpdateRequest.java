package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


/**
 * @author wx
 */
@Data
public class TestPlanApiCaseUpdateRequest extends BasePlanCaseBatchRequest {

    @Schema(description = "执行人id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.user_id.not_blank}")
    private String userId;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{api_definition.protocol.not_blank}")
    private List<String> protocols;
}
