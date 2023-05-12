package io.metersphere.sdk.domain;

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

@ApiModel(value = "环境组")
@TableName("environment_group")
@Data
public class EnvironmentGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 环境组id */
    @TableId
    @NotBlank(message = "环境组id不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "环境组id")
    private String id;
    
    /** 环境组名 */
    @Size(min = 1, max = 50, message = "环境组名长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "环境组名不能为空", groups = {Created.class})
    @ApiModelProperty(name = "环境组名")
    private String name;
    
    /** 所属工作空间 */
    @Size(min = 1, max = 64, message = "所属工作空间长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "所属工作空间不能为空", groups = {Created.class})
    @ApiModelProperty(name = "所属工作空间")
    private String workspaceId;
    
    /** 环境组描述 */
    
    
    @ApiModelProperty(name = "环境组描述")
    private String description;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    

}