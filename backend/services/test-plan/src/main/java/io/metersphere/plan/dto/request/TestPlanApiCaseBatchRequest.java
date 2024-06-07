package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanApiCaseBatchRequest extends BasePlanCaseBatchRequest {
    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols;
}
