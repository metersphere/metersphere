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

@ApiModel(value = "执行步骤基础信息(待讨论)")
@TableName("ui_scenario_execution_info")
@Data
public class UiScenarioExecutionInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{ui_scenario_execution_info.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 255, message = "{ui_scenario_execution_info.source_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_execution_info.source_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "api scenario id", required = true, allowableValues="range[1, 255]")
    private String sourceId;
    
    @Size(min = 1, max = 50, message = "{ui_scenario_execution_info.result.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_execution_info.result.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "", required = true, allowableValues="range[1, 50]")
    private String result;
    
    
    @ApiModelProperty(name = "", required = false, allowableValues="range[1, 50]")
    private String triggerMode;
    
    
    @ApiModelProperty(name = "Create timestamp", required = true, dataType = "Long")
    private Long createTime;
    

}