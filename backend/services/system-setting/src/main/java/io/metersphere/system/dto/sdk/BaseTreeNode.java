package io.metersphere.system.dto.sdk;

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

    @Schema(description = "父节点ID")
    private String parentId;

    @Schema(description = "子节点")
    private List<BaseTreeNode> children = new ArrayList<>();

    @Schema(description = "节点资源数量（多数情况下不会随着节点信息返回，视接口而定）")
    private long count = 0;

    public BaseTreeNode(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public BaseTreeNode(String id, String name, String type, String parentId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parentId = parentId;
    }

    public void addChild(BaseTreeNode node) {
        children.add(node);
    }
}
