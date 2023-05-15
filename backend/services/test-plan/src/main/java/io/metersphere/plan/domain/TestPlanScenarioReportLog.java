package io.metersphere.plan.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "测试计划场景报告过程日志")
@TableName("test_plan_scenario_report_log")
@Data
public class TestPlanScenarioReportLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{test_plan_scenario_report_log.test_plan_scenario_report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "请求资源 id", required = true, allowableValues="range[1, 50]")
    private String testPlanScenarioReportId;

    @ApiModelProperty(name = "执行日志", dataType = "byte[]")
    private byte[] console;
}