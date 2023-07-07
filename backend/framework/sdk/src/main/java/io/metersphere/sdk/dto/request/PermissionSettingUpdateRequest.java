package io.metersphere.sdk.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author jianxing
 */
@Data
public class PermissionSettingUpdateRequest {
    @Schema(title = "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String userRoleId;
    @NotNull
    @Schema(title = "菜单下的权限列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<PermissionUpdateRequest> permissions;

    @Data
    public static class PermissionUpdateRequest {
        @NotBlank
        @Schema(title = "权限ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private String id;
        @Schema(title = "是否启用该权限", requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean enable = false;
    }
}
