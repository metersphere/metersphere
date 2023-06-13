package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author jianxing
 */
@Data
public class PermissionSettingUpdateRequest {
    @Schema(title = "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userRoleId;
    @Schema(title = "菜单下的权限列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PermissionUpdateDTO> permissions;

    @Data
    class PermissionUpdateDTO {
        @Schema(title = "权限ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private String id;
        @Schema(title = "是否启用该权限", requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean enable = false;
    }
}
