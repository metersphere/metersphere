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

@ApiModel(value = "环境组配置")
@TableName("environment_group_project")
@Data
public class EnvironmentGroupProject implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String id;
    
    /** 环境组id */
    
    
    @ApiModelProperty(name = "环境组id")
    private String environmentGroupId;
    
    /** api test environment 环境ID */
    
    
    @ApiModelProperty(name = "api test environment 环境ID")
    private String environmentId;
    
    /** 项目id */
    
    
    @ApiModelProperty(name = "项目id")
    private String projectId;
    

}