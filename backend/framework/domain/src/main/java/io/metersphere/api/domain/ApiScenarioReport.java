package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReport implements Serializable {
    @Schema(title = "场景报告pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_scenario_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "删除标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.deleted.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_scenario_report.deleted.length_range}", groups = {Created.class, Updated.class})
    private Boolean deleted;

    @Schema(title = "修改人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUser;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "通过率")
    private Long passRate;

    @Schema(title = "报告状态/SUCCESS/ERROR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "触发方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.trigger_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(title = "执行模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.run_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    private String runMode;

    @Schema(title = "资源池", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    private String poolId;

    @Schema(title = "版本fk")
    private String versionId;

    @Schema(title = "是否是集成报告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.integrated.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_scenario_report.integrated.length_range}", groups = {Created.class, Updated.class})
    private Boolean integrated;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "场景fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String scenarioId;

    private static final long serialVersionUID = 1L;
}