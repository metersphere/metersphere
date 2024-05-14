package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

@Data
public class TestPlanReportFunctionCase implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "测试计划报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.test_plan_report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.test_plan_report_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanReportId;

    @Schema(description = "测试计划功能用例关联ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.test_plan_function_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.test_plan_function_case_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanFunctionCaseId;

    @Schema(description = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.function_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.function_case_id.length_range}", groups = {Created.class, Updated.class})
    private String functionCaseId;

    @Schema(description = "执行结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.execute_result.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.execute_result.length_range}", groups = {Created.class, Updated.class})
    private String executeResult;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        testPlanReportId("test_plan_report_id", "testPlanReportId", "VARCHAR", false),
        testPlanFunctionCaseId("test_plan_function_case_id", "testPlanFunctionCaseId", "VARCHAR", false),
        functionCaseId("function_case_id", "functionCaseId", "VARCHAR", false),
        executeResult("execute_result", "executeResult", "VARCHAR", false);

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