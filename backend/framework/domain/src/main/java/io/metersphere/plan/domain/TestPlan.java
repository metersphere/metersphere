package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlan implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "测试计划所属项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "测试计划父ID;测试计划要改为树结构。最上层的为root，其余则是父节点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(description =  "测试计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "测试计划状态;进行中/未开始/已完成/已结束/已归档", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{test_plan.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description =  "测试阶段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.stage.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{test_plan.stage.length_range}", groups = {Created.class, Updated.class})
    private String stage;

    @Schema(description =  "标签")
    private String tags;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "更新人")
    private String updateUser;

    @Schema(description =  "计划开始时间")
    private Long plannedStartTime;

    @Schema(description =  "计划结束时间")
    private Long plannedEndTime;

    @Schema(description =  "实际开始时间")
    private Long actualStartTime;

    @Schema(description =  "实际结束时间")
    private Long actualEndTime;

    @Schema(description =  "描述")
    private String description;

    private static final long serialVersionUID = 1L;
}