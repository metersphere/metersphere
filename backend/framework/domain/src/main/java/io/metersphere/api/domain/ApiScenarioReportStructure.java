
package io.metersphere.api.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "场景报告结构")
@Table("api_scenario_report_structure")
@Data
public class ApiScenarioReportStructure implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_report_structure.report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "请求资源 id", required = true, allowableValues = "range[1, 50]")
    private String reportId;

    @ApiModelProperty(name = "资源步骤结构树", required = false, dataType = "byte[]")
    private byte[] resourceTree;

}