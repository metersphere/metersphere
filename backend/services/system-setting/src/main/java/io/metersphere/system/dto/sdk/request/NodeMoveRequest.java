package io.metersphere.system.dto.sdk.request;

import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class NodeMoveRequest {
    @Schema(description = "被拖拽的节点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{node.not_blank}")
    private String dragNodeId;

    @Schema(description = "放入的节点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{node.not_blank}")
    private String dropNodeId;

    @Schema(description = "放入的位置（取值：-1，,1。  -1：dropNodeId节点之前。 1：dropNodeId节点后）", requiredMode = Schema.RequiredMode.REQUIRED)
    private int dropPosition;

    public void setAndConvertDropPosition(String position, boolean isSortDesc) {
        if (StringUtils.equals(MoveTypeEnum.BEFORE.name(), position)) {
            this.dropPosition = isSortDesc ? 1 : -1;
        } else {
            this.dropPosition = isSortDesc ? -1 : 1;
        }
    }
}

