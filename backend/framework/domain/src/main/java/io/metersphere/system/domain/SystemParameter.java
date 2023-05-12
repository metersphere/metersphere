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

@ApiModel(value = "系统参数")
@TableName("system_parameter")
@Data
public class SystemParameter implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 参数名称 */
    @TableId
    @NotBlank(message = "参数名称不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "参数名称")
    private String paramKey;
    
    /** 参数值 */
    
    
    @ApiModelProperty(name = "参数值")
    private String paramValue;
    
    /** 类型 */
    @Size(min = 1, max = 100, message = "类型长度必须在1-100之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "类型")
    private String type;
    

}