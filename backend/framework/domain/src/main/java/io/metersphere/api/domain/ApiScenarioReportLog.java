
package io.metersphere.api.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "场景报告过程日志")
@TableName("api_scenario_report_log")
@Data
public class ApiScenarioReportLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_scenario_report_log.report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "请求资源 id", required = true, allowableValues = "range[1, 50]")
    private String reportId;

    @ApiModelProperty(name = "执行日志", required = false, dataType = "byte[]")
    private byte[] console;

}