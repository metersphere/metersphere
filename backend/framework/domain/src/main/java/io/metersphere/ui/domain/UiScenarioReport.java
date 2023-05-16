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

@ApiModel(value = "场景报告")
@TableName("ui_scenario_report")
@Data
public class UiScenarioReport implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{ui_scenario_report.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "报告ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{ui_scenario_report.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 50]")
    private String projectId;
    
    @Size(min = 1, max = 3000, message = "{ui_scenario_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告名称", required = true, allowableValues="range[1, 3000]")
    private String name;
    
    
    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;
    
    
    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;
    
    @Size(min = 1, max = 64, message = "{ui_scenario_report.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告状态", required = true, allowableValues="range[1, 64]")
    private String status;
    
    @Size(min = 1, max = 64, message = "{ui_scenario_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report.trigger_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "触发模式（手动，定时，批量，测试计划）", required = true, allowableValues="range[1, 64]")
    private String triggerMode;
    
    
    @ApiModelProperty(name = "执行类型（并行，串行）", required = false, allowableValues="range[1, 200]")
    private String executeType;
    
    
    @ApiModelProperty(name = "场景名称", required = false, allowableValues="range[1, 3000]")
    private String scenarioName;
    
    
    @ApiModelProperty(name = "场景ID", required = false, allowableValues="range[1, 3000]")
    private String scenarioId;
    
    @Size(min = 1, max = 100, message = "{ui_scenario_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 100]")
    private String createUser;
    
    
    @ApiModelProperty(name = "执行器", required = false, allowableValues="range[1, 100]")
    private String actuator;
    
    
    @ApiModelProperty(name = "结束时间", required = true, dataType = "Long")
    private Long endTime;
    
    
    @ApiModelProperty(name = "报告类型（集合，独立）", required = false, allowableValues="range[1, 100]")
    private String reportType;
    
    
    @ApiModelProperty(name = "关联的测试计划报告ID（可以为空)", required = false, allowableValues="range[1, 50]")
    private String relevanceTestPlanReportId;
    

}