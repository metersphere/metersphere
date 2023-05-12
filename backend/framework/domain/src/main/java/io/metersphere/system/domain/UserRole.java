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

@ApiModel(value = "用户组")
@TableName("user_role")
@Data
public class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 组ID */
    @TableId
    @NotBlank(message = "组ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "组ID")
    private String id;
    
    /** 组名称 */
    @Size(min = 1, max = 64, message = "组名称长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "组名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "组名称")
    private String name;
    
    /** 描述 */
    
    
    @ApiModelProperty(name = "描述")
    private String description;
    
    /** 是否是系统用户组 */
    @Size(min = 1, max = 1, message = "是否是系统用户组长度必须在1-1之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "是否是系统用户组不能为空", groups = {Created.class})
    @ApiModelProperty(name = "是否是系统用户组")
    private Boolean system;
    
    /** 所属类型 */
    @Size(min = 1, max = 20, message = "所属类型长度必须在1-20之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "所属类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "所属类型")
    private String type;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 创建人(操作人） */
    @Size(min = 1, max = 64, message = "创建人(操作人）长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "创建人(操作人）不能为空", groups = {Created.class})
    @ApiModelProperty(name = "创建人(操作人）")
    private String createUser;
    
    /** 应用范围 */
    @Size(min = 1, max = 64, message = "应用范围长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "应用范围不能为空", groups = {Created.class})
    @ApiModelProperty(name = "应用范围")
    private String scopeId;
    

}