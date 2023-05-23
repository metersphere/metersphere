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

@ApiModel(value = "场景引用关系")
@Table("ui_scenario_reference")
@Data
public class UiScenarioReference implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{ui_scenario_reference.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 255, message = "{ui_scenario_reference.ui_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_reference.ui_scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景ID", required = true, allowableValues="range[1, 255]")
    private String uiScenarioId;
    
    
    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;
    
    @Size(min = 1, max = 64, message = "{ui_scenario_reference.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_reference.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 64]")
    private String createUser;
    
    @Size(min = 1, max = 255, message = "{ui_scenario_reference.reference_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_reference.reference_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "被引用的ID", required = true, allowableValues="range[1, 255]")
    private String referenceId;
    
    @Size(min = 1, max = 255, message = "{ui_scenario_reference.data_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_reference.data_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "引用的数据类型（场景，指令）", required = true, allowableValues="range[1, 255]")
    private String dataType;
    

}