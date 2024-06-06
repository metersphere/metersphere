package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TestPlanCollectionMinderTreeNodeDTO {

    @Schema(description = "节点ID")
    private String id;

    @Schema(description = "节点顺序")
    private Long pos;

    @Schema(description = "节点名称")
    private String text;

    @Schema(description = "数量")
    private int num;

    @Schema(description = "串并行")
    private String priority;

    @Schema(description = "串并行值")
    private String executeMethod;

    @Schema(description = "节点标签")
    private List<String> resource;

    @Schema(description = "节点状态")
    private String expandState = "expand";

}
