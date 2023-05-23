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

@ApiModel(value = "测试计划关联场景用例")
@Table("test_plan_api_scenario")
@Data
public class TestPlanApiScenario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_api_scenario.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_api_scenario.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_scenario.test_plan_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 255, message = "{test_plan_api_scenario.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_scenario.api_scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景ID", required = true, allowableValues = "range[1, 255]")
    private String apiScenarioId;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 100, message = "{test_plan_api_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_scenario.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "自定义排序，间隔5000", required = true, dataType = "Long")
    private Long pos;


    @ApiModelProperty(name = "环境类型", required = false, allowableValues = "range[1, 20]")
    private String environmentType;


    @ApiModelProperty(name = "所属环境", required = false, allowableValues = "range[1, ]")
    private String environment;


    @ApiModelProperty(name = "环境组ID", required = false, allowableValues = "range[1, 50]")
    private String environmentGroupId;

}