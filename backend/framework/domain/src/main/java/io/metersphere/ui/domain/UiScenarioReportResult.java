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

@ApiModel(value = "报告结果")
@Table("ui_scenario_report_result")
@Data
public class UiScenarioReportResult implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{ui_scenario_report_result.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 200, message = "{ui_scenario_report_result.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report_result.resource_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "请求资源 id", required = true, allowableValues="range[1, 200]")
    private String resourceId;
    
    @Size(min = 1, max = 50, message = "{ui_scenario_report_result.report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report_result.report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告 id", required = true, allowableValues="range[1, 50]")
    private String reportId;
    
    
    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;
    
    @Size(min = 1, max = 100, message = "{ui_scenario_report_result.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report_result.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "结果状态", required = true, allowableValues="range[1, 100]")
    private String status;
    
    
    @ApiModelProperty(name = "请求时间", required = true, dataType = "Long")
    private Long requestTime;
    
    
    @ApiModelProperty(name = "总断言数", required = false, dataType = "Long")
    private Long totalAssertions;
    
    
    @ApiModelProperty(name = "失败断言数", required = false, dataType = "Long")
    private Long passAssertions;
    
    
    @ApiModelProperty(name = "执行结果", required = true, allowableValues="range[1, ]")
    private byte[] content;
    
    
    @ApiModelProperty(name = "记录截图断言等结果", required = false, allowableValues="range[1, ]")
    private byte[] baseInfo;
    

}