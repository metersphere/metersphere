
package io.metersphere.api.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "场景报告结构")
@TableName("api_scenario_report_structure")
@Data
public class ApiScenarioReportStructure implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_scenario_report_structure.report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "请求资源 id", required = true, allowableValues = "range[1, 50]")
    private String reportId;

    @ApiModelProperty(name = "资源步骤结构树", required = false, dataType = "byte[]")
    private byte[] resourceTree;

}