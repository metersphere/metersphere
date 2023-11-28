package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FunctionalDemandDTO  extends FunctionalCaseDemand {
    @Schema(description = "同平台需求展开项")
    private List<FunctionalCaseDemand> children;
}
