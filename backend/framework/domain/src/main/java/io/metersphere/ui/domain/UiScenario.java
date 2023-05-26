package io.metersphere.ui.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenario implements Serializable {
    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.module_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(title = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{ui_scenario.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.level.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 100, message = "{ui_scenario.level.length_range}", groups = {Created.class, Updated.class})
    private String level;

    @Schema(title = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.status.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 100, message = "{ui_scenario.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "责任人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.principal.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.principal.length_range}", groups = {Created.class, Updated.class})
    private String principal;

    @Schema(title = "步骤总数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.step_total.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 10, message = "{ui_scenario.step_total.length_range}", groups = {Created.class, Updated.class})
    private Integer stepTotal;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "最后执行结果")
    private String lastResult;

    @Schema(title = "最后执行结果的报告ID")
    private String reportId;

    @Schema(title = "num", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.num.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 10, message = "{ui_scenario.num.length_range}", groups = {Created.class, Updated.class})
    private Integer num;

    @Schema(title = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.deleted.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{ui_scenario.deleted.length_range}", groups = {Created.class, Updated.class})
    private Boolean deleted;

    @Schema(title = "自定义num")
    private String customNum;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.pos.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 19, message = "{ui_scenario.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(title = "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.version_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(title = "指向初始版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.ref_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(title = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.latest.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{ui_scenario.latest.length_range}", groups = {Created.class, Updated.class})
    private Boolean latest;

    @Schema(title = "描述")
    private String description;

    private static final long serialVersionUID = 1L;
}