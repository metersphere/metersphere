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

@ApiModel(value = "工作空间")
@TableName("workspace")
@Data
public class Workspace implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 工作空间ID */
    @TableId
    @NotBlank(message = "工作空间ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "工作空间ID")
    private String id;
    
    /** 工作空间名称 */
    @Size(min = 1, max = 100, message = "工作空间名称长度必须在1-100之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "工作空间名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "工作空间名称")
    private String name;
    
    /** 描述 */
    
    
    @ApiModelProperty(name = "描述")
    private String description;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 创建人 */
    @Size(min = 1, max = 50, message = "创建人长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "创建人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "创建人")
    private String createUser;
    

}