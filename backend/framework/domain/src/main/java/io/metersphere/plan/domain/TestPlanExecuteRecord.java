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

@ApiModel(value = "测试计划执行记录")
@Table("test_plan_execute_record")
@Data
public class TestPlanExecuteRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_execute_record.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试计划执行记录ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_execute_record.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execute_record.test_plan_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 255, message = "{test_plan_execute_record.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execute_record.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行记录名称", required = true, allowableValues = "range[1, 255]")
    private String name;

    @Size(min = 1, max = 50, message = "{test_plan_execute_record.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execute_record.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行状态", required = true, allowableValues = "range[1, 50]")
    private String status;


    @ApiModelProperty(name = "触发类型", allowableValues = "range[1, 50]")
    private String triggerMode;


    @ApiModelProperty(name = "创建人(执行人/触发人)", allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "创建时间(开始时间)", dataType = "Long")
    private Long createTime;


    @ApiModelProperty(name = "结束时间", dataType = "Long")
    private Long endTime;


    @ApiModelProperty(name = "用例总数量", dataType = "Integer")
    private Integer caseCount;


    @ApiModelProperty(name = "执行率", allowableValues = "range[1, 22]")
    private Double executeRate;


    @ApiModelProperty(name = "通过率", allowableValues = "range[1, 22]")
    private Double passRate;


}