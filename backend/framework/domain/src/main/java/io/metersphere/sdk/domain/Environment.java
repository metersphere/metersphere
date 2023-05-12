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

@ApiModel(value = "环境")
@TableName("environment")
@Data
public class Environment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Api Test Environment ID */
    @TableId
    @NotBlank(message = "Api Test Environment ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "Api Test Environment ID")
    private String id;
    
    /** Api Test Environment Name */
    @Size(min = 1, max = 64, message = "Api Test Environment Name长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Api Test Environment Name不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Api Test Environment Name")
    private String name;
    
    /** Project ID */
    @Size(min = 1, max = 50, message = "Project ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Project ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Project ID")
    private String projectId;
    
    /** Api Test Protocol */
    
    
    @ApiModelProperty(name = "Api Test Protocol")
    private String protocol;
    
    /** Api Test Socket */
    
    
    @ApiModelProperty(name = "Api Test Socket")
    private String socket;
    
    /** Api Test Domain */
    
    
    @ApiModelProperty(name = "Api Test Domain")
    private String domain;
    
    /** Api Test Port */
    
    
    @ApiModelProperty(name = "Api Test Port")
    private Integer port;
    
    /** Global ariables */
    
    
    @ApiModelProperty(name = "Global ariables")
    private String variables;
    
    /** Global Heards */
    
    
    @ApiModelProperty(name = "Global Heards")
    private String headers;
    
    /** Config Data (JSON format) */
    
    
    @ApiModelProperty(name = "Config Data (JSON format)")
    private String config;
    
    /** hosts */
    
    
    @ApiModelProperty(name = "hosts")
    private String hosts;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String createUser;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private Long createTime;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private Long updateTime;
    

}