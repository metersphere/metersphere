package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class FunctionalDemandDTO  extends FunctionalCaseDemand {
    @Schema(description = "同平台需求展开项")
    private List<FunctionalDemandDTO> children = new ArrayList<>();;

    public void addChild(FunctionalDemandDTO demandDTO) {
        children.add(demandDTO);
    }
}
