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

@ApiModel(value = "用户组关系")
@TableName("user_role_relation")
@Data
public class UserRoleRelation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 用户组关系ID */
    @TableId
    @NotBlank(message = "用户组关系ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "用户组关系ID")
    private String id;
    
    /** 用户ID */
    @Size(min = 1, max = 50, message = "用户ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "用户ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "用户ID")
    private String userId;
    
    /** 组ID */
    @Size(min = 1, max = 50, message = "组ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "组ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "组ID")
    private String roleId;
    
    /** 工作空间或项目ID */
    @Size(min = 1, max = 50, message = "工作空间或项目ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "工作空间或项目ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "工作空间或项目ID")
    private String sourceId;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    

}