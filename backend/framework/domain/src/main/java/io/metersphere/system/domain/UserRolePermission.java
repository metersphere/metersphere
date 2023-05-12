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
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String id;
    
    /** 用户组ID */
    @Size(min = 1, max = 64, message = "用户组ID长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "用户组ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "用户组ID")
    private String roleId;
    
    /** 权限ID */
    @Size(min = 1, max = 128, message = "权限ID长度必须在1-128之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "权限ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "权限ID")
    private String permissionId;
    
    /** 功能菜单 */
    @Size(min = 1, max = 64, message = "功能菜单长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "功能菜单不能为空", groups = {Created.class})
    @ApiModelProperty(name = "功能菜单")
    private String moduleId;
    

}