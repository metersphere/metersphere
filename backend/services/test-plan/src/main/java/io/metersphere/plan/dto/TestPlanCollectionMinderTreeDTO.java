package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TestPlanCollectionMinderTreeDTO {

    @Schema(description = "节点data数据")
    private TestPlanCollectionMinderTreeNodeDTO data;

    @Schema(description = "节点children数据")
    private List<TestPlanCollectionMinderTreeDTO> children = new ArrayList<>();
}
