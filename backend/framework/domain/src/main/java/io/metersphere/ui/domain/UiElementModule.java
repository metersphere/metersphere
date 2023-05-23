package io.metersphere.ui.domain;

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

@ApiModel(value = "元素模块")
@Table("ui_element_module")
@Data
public class UiElementModule implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{ui_element_module.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "模块ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{ui_element_module.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_module.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 50]")
    private String projectId;
    
    @Size(min = 1, max = 64, message = "{ui_element_module.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_module.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "模块名称", required = true, allowableValues="range[1, 64]")
    private String name;
    
    @Size(min = 1, max = 50, message = "{ui_element_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_module.parent_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "父级ID", required = true, allowableValues="range[1, 50]")
    private String parentId;
    
    
    @ApiModelProperty(name = "模块等级", required = true, dataType = "Integer")
    private Integer level;
    
    
    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;
    
    
    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;
    
    @Size(min = 1, max = 22, message = "{ui_element_module.pos.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_module.pos.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "自定义排序", required = true, allowableValues="range[1, 22]")
    private Double pos;
    
    @Size(min = 1, max = 100, message = "{ui_element_module.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_module.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 100]")
    private String createUser;
    

}