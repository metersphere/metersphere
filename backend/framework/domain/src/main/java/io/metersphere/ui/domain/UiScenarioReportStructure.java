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

@ApiModel(value = "报告结构")
@Table("ui_scenario_report_structure")
@Data
public class UiScenarioReportStructure implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{ui_scenario_report_structure.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{ui_scenario_report_structure.report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario_report_structure.report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "请求资源 id", required = true, allowableValues="range[1, 50]")
    private String reportId;
    
    
    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;
    
    
    @ApiModelProperty(name = "资源步骤结构树", required = true, allowableValues="range[1, ]")
    private byte[] resourceTree;
    
    
    @ApiModelProperty(name = "执行日志", required = true, allowableValues="range[1, ]")
    private byte[] console;
    

}