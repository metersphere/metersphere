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

@ApiModel(value = "测试计划报告")
@Table("test_plan_report")
@Data
public class TestPlanReport implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_report.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_report.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_report.test_plan_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 128, message = "{test_plan_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_report.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告名称", required = true, allowableValues = "range[1, 128]")
    private String name;

    @Size(min = 1, max = 50, message = "{test_plan_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_report.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;


    @ApiModelProperty(name = "修改人", allowableValues = "range[1, 50]")
    private String updateUser;


    @ApiModelProperty(name = "更新时间", dataType = "Long")
    private Long updateTime;


    @ApiModelProperty(name = "开始时间", dataType = "Long")
    private Long startTime;


    @ApiModelProperty(name = "结束时间", dataType = "Long")
    private Long endTime;


    @ApiModelProperty(name = "用例数量", dataType = "Long")
    private Long caseCount;


    @ApiModelProperty(name = "执行率", allowableValues = "range[1, 22]")
    private Double executeRate;


    @ApiModelProperty(name = "通过率", allowableValues = "range[1, 22]")
    private Double passRate;


}