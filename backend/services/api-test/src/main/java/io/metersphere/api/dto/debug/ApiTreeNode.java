package io.metersphere.api.dto.debug;

import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiTreeNode extends BaseTreeNode {
    @Schema(description = "方法")
    private String method;
    @Schema(description = "协议")
    private String protocol;

}
