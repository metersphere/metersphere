package io.metersphere.system.dto.sdk.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jianxing
 */
@Data
public class PermissionSettingUpdateRequest {
    @Schema(description =  "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String userRoleId;
    @NotNull
    @Schema(description =  "菜单下的权限列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<PermissionUpdateRequest> permissions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionUpdateRequest {
        @NotBlank
        @Schema(description =  "权限ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private String id;
        @Schema(description =  "是否启用该权限", requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean enable = false;
    }
}
