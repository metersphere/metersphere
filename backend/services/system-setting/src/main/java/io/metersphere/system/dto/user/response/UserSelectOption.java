package io.metersphere.system.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserSelectOption {
    @Schema(description = "节点唯一ID")
    private String id;
    @Schema(description = "节点名称")
    private String name;
    @Schema(description = "是否选中")
    private boolean selected = false;
    @Schema(description = "是否允许关闭")
    private boolean closeable = true;
}
