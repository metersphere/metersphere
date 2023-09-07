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
    private boolean leafNode = false;

    //    @Schema(description = "排序单位")
    //    private int pos;

    @Schema(description = "子节点")
    private List<BaseTreeNode> children = new ArrayList<>();

    public BaseTreeNode(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    //    public BaseTreeNode(String id, String name, String type, int pos) {
    //        this.id = id;
    //        this.name = name;
    //        this.type = type;
    //        this.pos = pos;
    //    }

    public void addChild(BaseTreeNode node) {
        this.leafNode = false;
        children.add(node);
    }
}
