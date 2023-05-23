package io.metersphere.project.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;

@ApiModel(value = "自定义字段")
@Table("custom_field")
@Data
public class CustomField implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @NotBlank(message = "{custom_field.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "自定义字段ID", required = true, allowableValues="range[1, 100]")
    private String id;
    
    @Size(min = 1, max = 64, message = "{custom_field.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_field.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "自定义字段名称", required = true, allowableValues="range[1, 64]")
    private String name;
    
    @Size(min = 1, max = 30, message = "{custom_field.scene.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_field.scene.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "使用场景", required = true, allowableValues="range[1, 30]")
    private String scene;
    
    @Size(min = 1, max = 30, message = "{custom_field.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_field.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "自定义字段类型", required = true, allowableValues="range[1, 30]")
    private String type;
    
    
    
    @ApiModelProperty(name = "自定义字段备注", required = false, allowableValues="range[1, 255]")
    private String remark;
    
    
    
    @ApiModelProperty(name = "自定义字段选项", required = false, allowableValues="range[1, ]")
    private String options;
    
    
    
    @ApiModelProperty(name = "是否是系统字段", required = false, allowableValues="range[1, ]")
    private Boolean system;
    
    
    
    @ApiModelProperty(name = "是否是全局字段", required = false, allowableValues="range[1, ]")
    private Boolean global;
    
    
    
    @ApiModelProperty(name = "创建时间", required = true, allowableValues="range[1, ]")
    private Long createTime;
    
    
    
    @ApiModelProperty(name = "更新时间", required = true, allowableValues="range[1, ]")
    private Long updateTime;
    
    
    
    @ApiModelProperty(name = "创建人", required = false, allowableValues="range[1, 100]")
    private String createUser;
    
    
    
    @ApiModelProperty(name = "项目ID", required = false, allowableValues="range[1, 64]")
    private String projectId;
    
    
    
    @ApiModelProperty(name = "是否关联第三方", required = true, allowableValues="range[1, ]")
    private Boolean thirdPart;
    

}