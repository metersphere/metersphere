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

@ApiModel(value = "用户")
@TableName("user")
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    @TableId
    @NotBlank(message = "用户ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "用户ID")
    private String id;
    
    /** 用户名 */
    @Size(min = 1, max = 64, message = "用户名长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "用户名不能为空", groups = {Created.class})
    @ApiModelProperty(name = "用户名")
    private String name;
    
    /** 用户邮箱 */
    @Size(min = 1, max = 64, message = "用户邮箱长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "用户邮箱不能为空", groups = {Created.class})
    @ApiModelProperty(name = "用户邮箱")
    private String email;
    
    /** 用户密码 */
    
    
    @ApiModelProperty(name = "用户密码")
    private String password;
    
    /** 用户状态，启用或禁用 */
    @Size(min = 1, max = 50, message = "用户状态，启用或禁用长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "用户状态，启用或禁用不能为空", groups = {Created.class})
    @ApiModelProperty(name = "用户状态，启用或禁用")
    private String status;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 语言 */
    
    
    @ApiModelProperty(name = "语言")
    private String language;
    
    /** 当前工作空间ID */
    
    
    @ApiModelProperty(name = "当前工作空间ID")
    private String lastWorkspaceId;
    
    /** 手机号 */
    
    
    @ApiModelProperty(name = "手机号")
    private String phone;
    
    /** 来源：LOCAL OIDC CAS */
    @Size(min = 1, max = 50, message = "来源：LOCAL OIDC CAS长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "来源：LOCAL OIDC CAS不能为空", groups = {Created.class})
    @ApiModelProperty(name = "来源：LOCAL OIDC CAS")
    private String source;
    
    /** 当前项目ID */
    
    
    @ApiModelProperty(name = "当前项目ID")
    private String lastProjectId;
    
    /** 创建人 */
    @Size(min = 1, max = 100, message = "创建人长度必须在1-100之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "创建人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "创建人")
    private String createUser;
    

}