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

@ApiModel(value = "自定义模版")
@TableName("custom_field_template")
@Data
public class CustomFieldTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 自定义模版ID */
    @TableId
    @NotBlank(message = "自定义模版ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "自定义模版ID")
    private String id;
    
    /** 自定义字段ID */
    @Size(min = 1, max = 50, message = "自定义字段ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "自定义字段ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "自定义字段ID")
    private String fieldId;
    
    /** 模版ID */
    @Size(min = 1, max = 50, message = "模版ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "模版ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "模版ID")
    private String templateId;
    
    /** 使用场景 */
    @Size(min = 1, max = 30, message = "使用场景长度必须在1-30之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "使用场景不能为空", groups = {Created.class})
    @ApiModelProperty(name = "使用场景")
    private String scene;
    
    /** 是否必填 */
    
    
    @ApiModelProperty(name = "是否必填")
    private Boolean required;
    
    /** 排序字段 */
    
    
    @ApiModelProperty(name = "排序字段")
    private Integer pos;
    
    /** 默认值 */
    
    
    @ApiModelProperty(name = "默认值")
    private byte[] defaultValue;
    
    /** 自定义数据 */
    
    
    @ApiModelProperty(name = "自定义数据")
    private String customData;
    
    /** 自定义表头 */
    
    
    @ApiModelProperty(name = "自定义表头")
    private String key;
    

}