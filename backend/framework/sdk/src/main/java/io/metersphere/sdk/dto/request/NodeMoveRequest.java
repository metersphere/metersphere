package io.metersphere.sdk.dto.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class NodeMoveRequest {
    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{node.not_blank}")
    private String nodeId;

    @Schema(description = "父模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{parent.node.not_blank}")
    private String parentId = ModuleConstants.ROOT_NODE_PARENT_ID;

    @Schema(description = "前一个节点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String previousNodeId;

    @Schema(description = "后一个节点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nextNodeId;
}

