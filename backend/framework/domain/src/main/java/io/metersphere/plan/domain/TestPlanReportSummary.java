package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

@Data
public class TestPlanReportSummary implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_summary.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_summary.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "功能用例数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_summary.functional_case_count.not_blank}", groups = {Created.class})
    private Long functionalCaseCount;

    @Schema(description = "接口用例数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_summary.api_case_count.not_blank}", groups = {Created.class})
    private Long apiCaseCount;

    @Schema(description = "场景用例数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_summary.api_scenario_count.not_blank}", groups = {Created.class})
    private Long apiScenarioCount;

    @Schema(description = "缺陷数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_summary.bug_count.not_blank}", groups = {Created.class})
    private Long bugCount;

    @Schema(description = "测试计划报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_summary.test_plan_report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_summary.test_plan_report_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanReportId;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_summary.plan_count.not_blank}", groups = {Created.class})
    private Long planCount;

    @Schema(title = "总结")
    private String summary;

    @Schema(description = "功能用例执行结果")
    private byte[] functionalExecuteResult;

    @Schema(description = "接口执行结果")
    private byte[] apiExecuteResult;

    @Schema(description = "场景执行结果")
    private byte[] scenarioExecuteResult;

    @Schema(description = "执行结果")
    private byte[] executeResult;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        functionalCaseCount("functional_case_count", "functionalCaseCount", "BIGINT", false),
        apiCaseCount("api_case_count", "apiCaseCount", "BIGINT", false),
        apiScenarioCount("api_scenario_count", "apiScenarioCount", "BIGINT", false),
        bugCount("bug_count", "bugCount", "BIGINT", false),
        testPlanReportId("test_plan_report_id", "testPlanReportId", "VARCHAR", false),
        planCount("plan_count", "planCount", "BIGINT", false),
        summary("summary", "summary", "LONGVARCHAR", false),
        functionalExecuteResult("functional_execute_result", "functionalExecuteResult", "LONGVARBINARY", false),
        apiExecuteResult("api_execute_result", "apiExecuteResult", "LONGVARBINARY", false),
        scenarioExecuteResult("scenario_execute_result", "scenarioExecuteResult", "LONGVARBINARY", false),
        executeResult("execute_result", "executeResult", "LONGVARBINARY", false);

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