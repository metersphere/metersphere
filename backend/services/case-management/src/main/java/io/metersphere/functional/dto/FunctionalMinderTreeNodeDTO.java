package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FunctionalMinderTreeNodeDTO {

    @Schema(description = "节点ID")
    private String id;

    @Schema(description = "节点名称")
    private String text;

    @Schema(description = "用例等级")
    private String priority;

    @Schema(description = "节点标签")
    private List<String> resource;

    @Schema(description = "节点标签")
    private String expandState = "expand";

}
