package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenario implements Serializable {
    @Schema(description =  "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_scenario.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "删除时间")
    private Long deleteTime;

    @Schema(description =  "删除人")
    private String deleteUser;

    @Schema(description =  "更新人")
    private String updateUser;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "场景级别/P0/P1等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.level.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{api_scenario.level.length_range}", groups = {Created.class, Updated.class})
    private String level;

    @Schema(description =  "场景状态/未规划/已完成 等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description =  "责任人/用户fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.principal.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario.principal.length_range}", groups = {Created.class, Updated.class})
    private String principal;

    @Schema(description =  "场景步骤总数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario.step_total.not_blank}", groups = {Created.class})
    private Integer stepTotal;

    @Schema(description =  "通过率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario.pass_rate.not_blank}", groups = {Created.class})
    private Long passRate;

    @Schema(description =  "最后一次执行的结果状态")
    private String reportStatus;

    @Schema(description =  "编号")
    private Integer num;

    @Schema(description =  "自定义id")
    private String customNum;

    @Schema(description =  "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description =  "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description =  "版本fk")
    private String versionId;

    @Schema(description =  "引用资源fk")
    private String refId;

    @Schema(description =  "是否为最新版本 0:否，1:是")
    private Boolean latest;

    @Schema(description =  "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "场景模块fk")
    private String apiScenarioModuleId;

    @Schema(description =  "最后一次执行的报告fk")
    private String reportId;

    @Schema(description =  "描述信息")
    private String description;

    @Schema(description =  "模块全路径/用于导入模块创建")
    private String modulePath;

    @Schema(description =  "标签")
    private String tags;

    @Schema(description =  "是否为环境组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario.grouped.not_blank}", groups = {Created.class})
    private Boolean grouped;

    private static final long serialVersionUID = 1L;
}