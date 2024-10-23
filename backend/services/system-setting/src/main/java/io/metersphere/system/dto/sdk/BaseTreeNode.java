package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "子节点")
    private List<BaseTreeNode> children = new ArrayList<>();

    @Schema(description = "附加信息")
    private Map<String, String> attachInfo = new HashMap<>();

    @Schema(description = "节点资源数量（多数情况下不会随着节点信息返回，视接口而定）")
    private long count = 0;

    @Schema(description = "节点路径（当前节点所在整棵树的路径）")
    private String path = "/";

    public void genModulePath(BaseTreeNode parentNode) {
        if (parentNode != null) {
            path = parentNode.getPath() + "/" + this.getName();
        } else {
            path = "/" + this.getName();
        }
    }

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
        node.setParentId(this.getId());
        children.add(node);
    }

    public void putAttachInfo(String key, String value) {
        attachInfo.put(key, value);
    }
}
