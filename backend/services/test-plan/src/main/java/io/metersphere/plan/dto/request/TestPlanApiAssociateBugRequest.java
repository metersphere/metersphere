package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanApiAssociateBugRequest extends TestPlanApiCaseBatchRequest {

    @Schema(description = "缺陷ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{bug.id.not_blank}")
    private List<String> bugIds;
}
