package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanResourceSortResponse {
    @Schema(description = "本次排序的数量")
    private long sortNodeNum;
}
