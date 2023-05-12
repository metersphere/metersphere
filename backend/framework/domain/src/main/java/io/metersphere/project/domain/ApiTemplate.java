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

@ApiModel(value = "API模版表")
@TableName("api_template")
@Data
public class ApiTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 模版ID */
    @TableId
    @NotBlank(message = "模版ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "模版ID")
    private String id;
    
    /** 模版名称 */
    @Size(min = 1, max = 64, message = "模版名称长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "模版名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "模版名称")
    private String name;
    
    /** 描述 */
    
    
    @ApiModelProperty(name = "描述")
    private String description;
    
    /** 是否是系统模版 */
    @Size(min = 1, max = 1, message = "是否是系统模版长度必须在1-1之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "是否是系统模版不能为空", groups = {Created.class})
    @ApiModelProperty(name = "是否是系统模版")
    private Boolean system;
    
    /** 是否是全局模版 */
    @Size(min = 1, max = 1, message = "是否是全局模版长度必须在1-1之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "是否是全局模版不能为空", groups = {Created.class})
    @ApiModelProperty(name = "是否是全局模版")
    private Boolean global;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 项目ID */
    
    
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    

}