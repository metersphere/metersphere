package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class BaseTreeNode {

    @Schema(description = "节点ID")
    private String id;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "节点类型")
    private String type;

    @Schema(description = "是否是叶子节点")
    private boolean leafNode;

    @Schema(description = "子节点")
    private List<BaseTreeNode> children = new ArrayList<>();

    public BaseTreeNode(String id, String name, String type, boolean isLeafNode) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.leafNode = isLeafNode;
    }

    public void addChild(BaseTreeNode node) {
        this.leafNode = false;
        children.add(node);
    }
}
