package io.metersphere.plan.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * @author wx
 */
@Data
public class TestPlanApiCaseBatchMoveRequest extends TestPlanApiCaseBatchRequest {

    @Schema(description = "目标计划集id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{targetCollectionId.not_blank}")
    private String targetCollectionId;
}
