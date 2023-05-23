package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "测试计划关联UI场景")
@Table("test_plan_ui_scenario")
@Data
public class TestPlanUiScenario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_ui_scenario.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_ui_scenario.test_plan_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.ui_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_ui_scenario.ui_scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "UI场景ID", required = true, allowableValues = "range[1, 50]")
    private String uiScenarioId;

    @Size(min = 1, max = 50, message = "{test_plan_ui_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_ui_scenario.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;


    @ApiModelProperty(name = "排序，默认值5000", required = true, dataType = "Long")
    private Long pos;


    @ApiModelProperty(name = "环境类型", allowableValues = "range[1, 20]")
    private String environmentType;


    @ApiModelProperty(name = "所属环境")
    private String environment;


    @ApiModelProperty(name = "环境组ID", allowableValues = "range[1, 50]")
    private String environmentGroupId;


}