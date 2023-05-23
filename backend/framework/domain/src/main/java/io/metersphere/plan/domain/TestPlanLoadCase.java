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

@ApiModel(value = "测试计划关联性能测试用例")
@Table("test_plan_load_case")
@Data
public class TestPlanLoadCase implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_load_case.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_load_case.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_load_case.test_plan_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 50, message = "{test_plan_load_case.load_case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_load_case.load_case_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "性能用例ID", required = true, allowableValues = "range[1, 50]")
    private String loadCaseId;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 50, message = "{test_plan_load_case.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_load_case.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "所用测试资源池ID", allowableValues = "range[1, 50]")
    private String testResourcePoolId;


    @ApiModelProperty(name = "自定义排序，间隔5000", required = true, dataType = "Long")
    private Long pos;


    @ApiModelProperty(name = "压力配置")
    private String loadConfiguration;


    @ApiModelProperty(name = "高级配置")
    private String advancedConfiguration;


}