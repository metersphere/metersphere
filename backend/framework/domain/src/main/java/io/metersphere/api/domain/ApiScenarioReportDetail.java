
package io.metersphere.api.domain;

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

@ApiModel(value = "场景报告步骤结果")
@TableName("api_scenario_report_detail")
@Data
public class ApiScenarioReportDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_scenario_report_detail.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report_detail.report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告fk", required = true, allowableValues = "range[1, 50]")
    private String reportId;

    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report_detail.resource_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景中各个步骤请求唯一标识", required = true, allowableValues = "range[1, 50]")
    private String resourceId;

    @ApiModelProperty(name = "开始时间", required = false, dataType = "Long")
    private Long startTime;

    @ApiModelProperty(name = "结果状态", required = false, allowableValues = "range[1, 20]")
    private String status;

    @ApiModelProperty(name = "单个请求步骤时间", required = false, dataType = "Long")
    private Long requestTime;

    @ApiModelProperty(name = "总断言数", required = false, dataType = "Long")
    private Long assertionsTotal;

    @ApiModelProperty(name = "失败断言数", required = false, dataType = "Long")
    private Long passAssertionsTotal;

    @ApiModelProperty(name = "误报编号", required = false, allowableValues = "range[1, 200]")
    private String fakeCode;

    @ApiModelProperty(name = "请求名称", required = false, allowableValues = "range[1, 500]")
    private String requestName;

    @ApiModelProperty(name = "环境fk", required = false, allowableValues = "range[1, 50]")
    private String environmentId;

    @ApiModelProperty(name = "项目fk", required = false, allowableValues = "range[1, 50]")
    private String projectId;

    @ApiModelProperty(name = "失败总数", required = false, dataType = "Integer")
    private Integer errorTotal;

    @ApiModelProperty(name = "请求响应码", required = false, allowableValues = "range[1, 500]")
    private String code;

    @ApiModelProperty(name = "执行结果", required = false, dataType = "byte[]")
    private byte[] content;

}