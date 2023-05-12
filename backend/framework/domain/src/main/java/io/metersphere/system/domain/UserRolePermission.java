package io.metersphere.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "用户组权限")
@TableName("user_role_permission")
@Data
public class UserRolePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{user_role_permission.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 64]")
    private String id;

    @Size(min = 1, max = 64, message = "{user_role_permission.role_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role_permission.role_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用户组ID", required = true, allowableValues = "range[1, 64]")
    private String roleId;

    @Size(min = 1, max = 128, message = "{user_role_permission.permission_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role_permission.permission_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "权限ID", required = true, allowableValues = "range[1, 128]")
    private String permissionId;

    @Size(min = 1, max = 64, message = "{user_role_permission.module_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role_permission.module_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "功能菜单", required = true, allowableValues = "range[1, 64]")
    private String moduleId;

}