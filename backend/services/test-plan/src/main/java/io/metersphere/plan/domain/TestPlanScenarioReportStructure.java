package io.metersphere.plan.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "测试计划场景报告结构")
@TableName("test_plan_scenario_report_structure")
@Data
public class TestPlanScenarioReportStructure implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{test_plan_scenario_report_structure.test_plan_scenario_report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试计划场景报告 id", required = true, allowableValues="range[1, 50]")
    private String testPlanScenarioReportId;


    @ApiModelProperty(name = "场景步骤结构树", dataType = "byte[]")
    private byte[] resourceTree;
}