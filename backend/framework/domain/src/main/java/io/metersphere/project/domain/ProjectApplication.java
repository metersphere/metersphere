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

@ApiModel(value = "项目应用")
@TableName("project_application")
@Data
public class ProjectApplication implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 项目ID */
    @TableId
    @NotBlank(message = "项目ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    
    /** 配置项 */
    @TableId
    @NotBlank(message = "配置项不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "配置项")
    private String type;
    
    /** 配置值 */
    
    
    @ApiModelProperty(name = "配置值")
    private String typeValue;
    

}