package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanAssociationResponse {
    @Schema(description = "本次关联的数量")
    private long associationCount;
}
