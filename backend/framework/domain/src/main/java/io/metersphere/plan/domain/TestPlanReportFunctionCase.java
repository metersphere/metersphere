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
public class TestPlanReportFunctionCase implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "测试计划报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.test_plan_report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.test_plan_report_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanReportId;

    @Schema(description = "测试计划功能用例关联ID(同一计划下可重复关联, 暂时保留)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.test_plan_function_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.test_plan_function_case_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanFunctionCaseId;

    @Schema(description = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.function_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.function_case_id.length_range}", groups = {Created.class, Updated.class})
    private String functionCaseId;

    @Schema(description = "功能用例业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_function_case.function_case_num.not_blank}", groups = {Created.class})
    private Long functionCaseNum;

    @Schema(description = "功能用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.function_case_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan_report_function_case.function_case_name.length_range}", groups = {Created.class, Updated.class})
    private String functionCaseName;

    @Schema(description = "功能用例所属模块")
    private String functionCaseModule;

    @Schema(description = "功能用例用例等级")
    private String functionCasePriority;

    @Schema(description = "功能用例执行人")
    private String functionCaseExecuteUser;

    @Schema(description = "功能用例关联缺陷数")
    private Long functionCaseBugCount;

    @Schema(description = "执行结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.function_case_execute_result.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.function_case_execute_result.length_range}", groups = {Created.class, Updated.class})
    private String functionCaseExecuteResult;

    @Schema(description = "测试集ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_function_case.test_plan_collection_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_function_case.test_plan_collection_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanCollectionId;

    @Schema(description = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_function_case.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "执行报告ID")
    private String functionCaseExecuteReportId;

    @Schema(description = "测试计划名称")
    private String testPlanName;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        testPlanReportId("test_plan_report_id", "testPlanReportId", "VARCHAR", false),
        testPlanFunctionCaseId("test_plan_function_case_id", "testPlanFunctionCaseId", "VARCHAR", false),
        functionCaseId("function_case_id", "functionCaseId", "VARCHAR", false),
        functionCaseNum("function_case_num", "functionCaseNum", "BIGINT", false),
        functionCaseName("function_case_name", "functionCaseName", "VARCHAR", false),
        functionCaseModule("function_case_module", "functionCaseModule", "VARCHAR", false),
        functionCasePriority("function_case_priority", "functionCasePriority", "VARCHAR", false),
        functionCaseExecuteUser("function_case_execute_user", "functionCaseExecuteUser", "VARCHAR", false),
        functionCaseBugCount("function_case_bug_count", "functionCaseBugCount", "BIGINT", false),
        functionCaseExecuteResult("function_case_execute_result", "functionCaseExecuteResult", "VARCHAR", false),
        testPlanCollectionId("test_plan_collection_id", "testPlanCollectionId", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        functionCaseExecuteReportId("function_case_execute_report_id", "functionCaseExecuteReportId", "VARCHAR", false),
        testPlanName("test_plan_name", "testPlanName", "VARCHAR", false);

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