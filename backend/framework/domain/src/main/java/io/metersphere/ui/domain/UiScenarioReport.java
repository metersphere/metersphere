package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenarioReport implements Serializable {
    @Schema(title = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ui_scenario_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "报告状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{ui_scenario_report.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "触发模式（手动，定时，批量，测试计划）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.trigger_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{ui_scenario_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(title = "执行类型（并行，串行）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.execute_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 200, message = "{ui_scenario_report.execute_type.length_range}", groups = {Created.class, Updated.class})
    private String executeType;

    @Schema(title = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.scenario_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ui_scenario_report.scenario_name.length_range}", groups = {Created.class, Updated.class})
    private String scenarioName;

    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report.scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String scenarioId;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    private String poolId;

    @Schema(title = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario_report.end_time.not_blank}", groups = {Created.class})
    private Long endTime;

    @Schema(title = "报告类型（集合，独立）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario_report.integrated.not_blank}", groups = {Created.class})
    private Boolean integrated;

    @Schema(title = "关联的测试计划报告ID（可以为空)")
    private String relevanceTestPlanReportId;

    private static final long serialVersionUID = 1L;
}