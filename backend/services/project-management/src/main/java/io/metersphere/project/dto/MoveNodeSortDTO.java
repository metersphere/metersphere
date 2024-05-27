package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoveNodeSortDTO {
    @Schema(description = "排序范围ID")
    private String sortRangeId;

    @Schema(description = "要排序的节点")
    private DropNode sortNode;

    @Schema(description = "前一个节点")
    private DropNode previousNode;

    @Schema(description = "后一个节点")
    private DropNode nextNode;
}