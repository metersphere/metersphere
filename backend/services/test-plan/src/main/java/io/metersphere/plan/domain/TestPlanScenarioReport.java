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

@ApiModel(value = "测试计划场景报告")
@TableName("test_plan_scenario_report")
@Data
public class TestPlanScenarioReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{test_plan_scenario_report.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试计划场景报告ID", required = true, allowableValues="range[1, 50]")
    private String id;

    @Size(min = 1, max = 300, message = "{test_plan_scenario_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告名称", required = true, allowableValues="range[1, 300]")
    private String name;

    @Size(min = 1, max = 50, message = "{test_plan_scenario_report.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{test_plan_scenario_report.test_plan_api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.test_plan_api_scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划场景ID", required = true, allowableValues="range[1, 50]")
    private String testPlanApiScenarioId;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 50, message = "{test_plan_scenario_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 50]")
    private String createUser;

    @ApiModelProperty(name = "删除时间", dataType = "Long")
    private Long deleteTime;

    @ApiModelProperty(name = "删除人", allowableValues="range[1, 50]")
    private String deleteUser;

    @Size(min = 1, max = 1, message = "{test_plan_scenario_report.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "删除标识", required = true, allowableValues="range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 50, message = "{test_plan_scenario_report.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "修改人", required = true, allowableValues="range[1, 50]")
    private String updateUser;

    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;

    @ApiModelProperty(name = "通过率", allowableValues="range[1, 100]")
    private String passRate;

    @Size(min = 1, max = 20, message = "{test_plan_scenario_report.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告状态", required = true, allowableValues="range[1, 20]")
    private String status;

    @Size(min = 1, max = 20, message = "{test_plan_scenario_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.trigger_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "触发方式", required = true, allowableValues="range[1, 20]")
    private String triggerMode;

    @Size(min = 1, max = 20, message = "{test_plan_scenario_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.run_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行模式", required = true, allowableValues="range[1, 20]")
    private String runMode;

    @Size(min = 1, max = 50, message = "{test_plan_scenario_report.resource_pool.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.resource_pool.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源池", required = true, allowableValues="range[1, 50]")
    private String resourcePool;

    @Size(min = 1, max = 20, message = "{test_plan_scenario_report.execute_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_scenario_report.execute_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行方式", required = true, allowableValues="range[1, 20]")
    private String executeType;

    @ApiModelProperty(name = "版本ID", allowableValues="range[1, 50]")
    private String versionId;
}