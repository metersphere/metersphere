package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;

@Data
public class TestPlanAssociationRequest extends BaseAssociateCaseRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{test_plan.id.not_blank}")
    @Schema(description = "测试计划ID")
    private String testPlanId;
}
