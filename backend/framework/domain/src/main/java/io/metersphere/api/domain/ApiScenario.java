package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenario implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_scenario.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "更新人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.update_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario.update_user.length_range}", groups = {Created.class, Updated.class})
    private String updateUser;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "场景级别/P0/P1等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.level.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{api_scenario.level.length_range}", groups = {Created.class, Updated.class})
    private String level;

    @Schema(title = "场景状态/未规划/已完成 等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "责任人/用户fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.principal.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario.principal.length_range}", groups = {Created.class, Updated.class})
    private String principal;

    @Schema(title = "场景步骤总数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.step_total.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{api_scenario.step_total.length_range}", groups = {Created.class, Updated.class})
    private Integer stepTotal;

    @Schema(title = "通过率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.pass_rate.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 19, message = "{api_scenario.pass_rate.length_range}", groups = {Created.class, Updated.class})
    private Long passRate;

    @Schema(title = "最后一次执行的结果状态")
    private String reportStatus;

    @Schema(title = "编号")
    private Integer num;

    @Schema(title = "自定义id")
    private String customNum;

    @Schema(title = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.deleted.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_scenario.deleted.length_range}", groups = {Created.class, Updated.class})
    private Boolean deleted;

    @Schema(title = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.pos.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 19, message = "{api_scenario.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(title = "版本fk")
    private String versionId;

    @Schema(title = "引用资源fk")
    private String refId;

    @Schema(title = "是否为最新版本 0:否，1:是")
    private Boolean latest;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "场景模块fk")
    private String apiScenarioModuleId;

    @Schema(title = "最后一次执行的报告fk")
    private String reportId;

    @Schema(title = "描述信息")
    private String description;

    @Schema(title = "模块全路径/用于导入模块创建")
    private String modulePath;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "是否为环境组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.grouped.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_scenario.grouped.length_range}", groups = {Created.class, Updated.class})
    private Boolean grouped;

    private static final long serialVersionUID = 1L;
}