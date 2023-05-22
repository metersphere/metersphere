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
    
    @TableId
    @NotBlank(message = "{custom_field_template.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "自定义模版ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{custom_field_template.field_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_field_template.field_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "自定义字段ID", required = true, allowableValues="range[1, 50]")
    private String fieldId;
    
    @Size(min = 1, max = 50, message = "{custom_field_template.template_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_field_template.template_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "模版ID", required = true, allowableValues="range[1, 50]")
    private String templateId;
    
    @Size(min = 1, max = 30, message = "{custom_field_template.scene.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_field_template.scene.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "使用场景", required = true, allowableValues="range[1, 30]")
    private String scene;
    
    
    
    @ApiModelProperty(name = "是否必填", required = false, allowableValues="range[1, ]")
    private Boolean required;
    
    
    
    @ApiModelProperty(name = "排序字段", required = false, allowableValues="range[1, ]")
    private Integer pos;
    
    
    
    @ApiModelProperty(name = "默认值", required = false, allowableValues="range[1, ]")
    private byte[] defaultValue;
    
    
    
    @ApiModelProperty(name = "自定义数据", required = false, allowableValues="range[1, 255]")
    private String customData;
    
    
    
    @ApiModelProperty(name = "自定义表头", required = false, allowableValues="range[1, 1]")
    private String key;
    

}