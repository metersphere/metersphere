package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssociationNodeSortDTO {
    @Schema(description = "测试计划ID")
    private String testPlanId;

    @Schema(description = "要排序的节点")
    private AssociationNode sortNode;

    @Schema(description = "前一个节点")
    private AssociationNode previousNode;

    @Schema(description = "后一个节点")
    private AssociationNode nextNode;

}

