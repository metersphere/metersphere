package io.metersphere.plan.domain;

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
@TableName("test_plan_scenario_report_detail")
@Data
public class TestPlanScenarioReportDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{test_plan_scenario_report_detail.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_scenario_report_detail.test_plan_scenario_report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report_detail.test_plan_scenario_report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划场景报告ID", required = true, allowableValues="range[1, 50]")
    private String testPlanScenarioReportId;

    @Size(min = 1, max = 50, message = "{test_plan_scenario_report_detail.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report_detail.resource_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景中各个步骤请求唯一标识", required = true, allowableValues="range[1, 50]")
    private String resourceId;

    @ApiModelProperty(name = "创建时间", dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "结果状态", allowableValues="range[1, 20]")
    private String status;

    @ApiModelProperty(name = "单个请求步骤时间", dataType = "Long")
    private Long requestTime;

    @ApiModelProperty(name = "总断言数", dataType = "Long")
    private Long totalAssertions;

    @ApiModelProperty(name = "失败断言数", dataType = "Long")
    private Long passAssertions;

    @ApiModelProperty(name = "误报编号", allowableValues="range[1, 255]")
    private String errorCode;

    @ApiModelProperty(name = "执行结果", dataType = "byte[]")
    private byte[] content;

    @ApiModelProperty(name = "基础信息", dataType = "byte[]")
    private byte[] baseInfo;

    @ApiModelProperty(name = "执行环境", dataType = "byte[]")
    private byte[] config;
}