package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class UserRolePermission implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_permission.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{user_role_permission.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_permission.role_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{user_role_permission.role_id.length_range}", groups = {Created.class, Updated.class})
    private String roleId;

    @Schema(title = "权限ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_permission.permission_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 128, message = "{user_role_permission.permission_id.length_range}", groups = {Created.class, Updated.class})
    private String permissionId;

    @Schema(title = "功能菜单", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_permission.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{user_role_permission.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    private static final long serialVersionUID = 1L;
}