package io.metersphere.project.domain;

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

@ApiModel(value = "项目")
@TableName("project")
@Data
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 项目ID */
    @TableId
    @NotBlank(message = "项目ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "项目ID")
    private String id;
    
    /** 工作空间ID */
    @Size(min = 1, max = 50, message = "工作空间ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "工作空间ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "工作空间ID")
    private String workspaceId;
    
    /** 项目名称 */
    @Size(min = 1, max = 64, message = "项目名称长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "项目名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "项目名称")
    private String name;
    
    /** 项目描述 */
    
    
    @ApiModelProperty(name = "项目描述")
    private String description;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String systemId;
    

}