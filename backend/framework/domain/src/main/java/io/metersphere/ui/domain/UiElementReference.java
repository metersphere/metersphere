package io.metersphere.ui.domain;

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

@ApiModel(value = "元素关系表")
@TableName("ui_element_reference")
@Data
public class UiElementReference implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{ui_element_reference.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 255, message = "{ui_element_reference.element_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_reference.element_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "元素ID", required = true, allowableValues="range[1, 255]")
    private String elementId;
    
    @Size(min = 1, max = 255, message = "{ui_element_reference.element_module_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_reference.element_module_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "元素模块ID", required = true, allowableValues="range[1, 255]")
    private String elementModuleId;
    
    @Size(min = 1, max = 255, message = "{ui_element_reference.scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_reference.scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景ID", required = true, allowableValues="range[1, 255]")
    private String scenarioId;
    
    @Size(min = 1, max = 50, message = "{ui_element_reference.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element_reference.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 50]")
    private String projectId;
    

}