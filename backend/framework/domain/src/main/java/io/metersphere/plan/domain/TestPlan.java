package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlan implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "测试计划所属项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "测试计划父ID;测试计划要改为树结构。最上层的为root，其余则是父节点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(title = "测试计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "测试计划状态;进行中/未开始/已完成/已结束/已归档", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{test_plan.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "测试阶段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.stage.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{test_plan.stage.length_range}", groups = {Created.class, Updated.class})
    private String stage;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "更新人")
    private String updateUser;

    @Schema(title = "计划开始时间")
    private Long plannedStartTime;

    @Schema(title = "计划结束时间")
    private Long plannedEndTime;

    @Schema(title = "实际开始时间")
    private Long actualStartTime;

    @Schema(title = "实际结束时间")
    private Long actualEndTime;

    @Schema(title = "描述")
    private String description;

    private static final long serialVersionUID = 1L;
}