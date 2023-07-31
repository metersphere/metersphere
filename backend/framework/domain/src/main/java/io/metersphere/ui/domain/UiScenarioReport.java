package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Schema(title = "执行类型（并行，串行）")
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

    @Schema(title = "报告类型（集合，独立）")
    private Boolean integrated;

    @Schema(title = "关联的测试计划报告ID（可以为空)")
    private String relevanceTestPlanReportId;

    @Schema(title = "报告生成来源(生成报告，后端调试，本地调试)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report.report_source.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{ui_scenario_report.report_source.length_range}", groups = {Created.class, Updated.class})
    private String reportSource;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        status("status", "status", "VARCHAR", true),
        triggerMode("trigger_mode", "triggerMode", "VARCHAR", false),
        executeType("execute_type", "executeType", "VARCHAR", false),
        scenarioName("scenario_name", "scenarioName", "VARCHAR", false),
        scenarioId("scenario_id", "scenarioId", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        poolId("pool_id", "poolId", "VARCHAR", false),
        endTime("end_time", "endTime", "BIGINT", false),
        integrated("integrated", "integrated", "BIT", false),
        relevanceTestPlanReportId("relevance_test_plan_report_id", "relevanceTestPlanReportId", "VARCHAR", false),
        reportSource("report_source", "reportSource", "VARCHAR", false);

        private static final String BEGINNING_DELIMITER = "`";

        private static final String ENDING_DELIMITER = "`";

        private final String column;

        private final boolean isColumnNameDelimited;

        private final String javaProperty;

        private final String jdbcType;

        public String value() {
            return this.column;
        }

        public String getValue() {
            return this.column;
        }

        public String getJavaProperty() {
            return this.javaProperty;
        }

        public String getJdbcType() {
            return this.jdbcType;
        }

        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        public static Column[] excludes(Column ... excludes) {
            ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new Column[]{});
        }

        public static Column[] all() {
            return Column.values();
        }

        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }

        public String getAliasedEscapedColumnName() {
            return this.getEscapedColumnName();
        }
    }
}