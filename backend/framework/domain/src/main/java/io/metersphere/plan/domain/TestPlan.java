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

@ApiModel(value = "测试计划")
@Table("test_plan")
@Data
public class TestPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划所属项目", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{test_plan.parent_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan.parent_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划父ID", required = true, allowableValues = "range[1, 50]")
    private String parentId;

    @Size(min = 1, max = 255, message = "{test_plan.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划名称", required = true, allowableValues = "range[1, 255]")
    private String name;

    @Size(min = 1, max = 20, message = "{test_plan.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划状态", required = true, allowableValues = "range[1, 20]")
    private String status;

    @Size(min = 1, max = 30, message = "{test_plan.stage.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan.stage.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试阶段", required = true, allowableValues = "range[1, 30]")
    private String stage;


    @ApiModelProperty(name = "标签", allowableValues = "range[1, 500]")
    private String tags;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 50, message = "{test_plan.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "更新时间", dataType = "Long")
    private Long updateTime;


    @ApiModelProperty(name = "更新人", allowableValues = "range[1, 50]")
    private String updateUser;


    @ApiModelProperty(name = "计划开始时间", dataType = "Long")
    private Long plannedStartTime;


    @ApiModelProperty(name = "计划结束时间", dataType = "Long")
    private Long plannedEndTime;


    @ApiModelProperty(name = "实际开始时间", dataType = "Long")
    private Long actualStartTime;


    @ApiModelProperty(name = "实际结束时间", dataType = "Long")
    private Long actualEndTime;


    @ApiModelProperty(name = "描述", allowableValues = "range[1, 2000]")
    private String description;


}