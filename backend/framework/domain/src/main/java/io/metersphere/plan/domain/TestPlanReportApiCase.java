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
public class TestPlanReportApiCase implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_api_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_api_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "测试计划报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_api_case.test_plan_report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_api_case.test_plan_report_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanReportId;

    @Schema(description = "测试集ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_api_case.test_plan_collection_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_api_case.test_plan_collection_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanCollectionId;

    @Schema(description = "环境ID")
    private String environmentId;

    @Schema(description = "测试计划接口用例关联ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_api_case.test_plan_api_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_api_case.test_plan_api_case_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanApiCaseId;

    @Schema(description = "接口用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_api_case.api_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report_api_case.api_case_id.length_range}", groups = {Created.class, Updated.class})
    private String apiCaseId;

    @Schema(description = "接口用例业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_api_case.api_case_num.not_blank}", groups = {Created.class})
    private Long apiCaseNum;

    @Schema(description = "接口用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report_api_case.api_case_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan_report_api_case.api_case_name.length_range}", groups = {Created.class, Updated.class})
    private String apiCaseName;

    @Schema(description = "接口用例所属模块")
    private String apiCaseModule;

    @Schema(description = "接口用例等级")
    private String apiCasePriority;

    @Schema(description = "接口用例执行人")
    private String apiCaseExecuteUser;

    @Schema(description = "接口用例执行结果")
    private String apiCaseExecuteResult;

    @Schema(description = "接口用例执行报告ID")
    private String apiCaseExecuteReportId;

    @Schema(description = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_api_case.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "测试计划名称")
    private String testPlanName;

    @Schema(description = "接口用例关联缺陷数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report_api_case.api_case_bug_count.not_blank}", groups = {Created.class})
    private Long apiCaseBugCount;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        testPlanReportId("test_plan_report_id", "testPlanReportId", "VARCHAR", false),
        testPlanCollectionId("test_plan_collection_id", "testPlanCollectionId", "VARCHAR", false),
        environmentId("environment_id", "environmentId", "VARCHAR", false),
        testPlanApiCaseId("test_plan_api_case_id", "testPlanApiCaseId", "VARCHAR", false),
        apiCaseId("api_case_id", "apiCaseId", "VARCHAR", false),
        apiCaseNum("api_case_num", "apiCaseNum", "BIGINT", false),
        apiCaseName("api_case_name", "apiCaseName", "VARCHAR", false),
        apiCaseModule("api_case_module", "apiCaseModule", "VARCHAR", false),
        apiCasePriority("api_case_priority", "apiCasePriority", "VARCHAR", false),
        apiCaseExecuteUser("api_case_execute_user", "apiCaseExecuteUser", "VARCHAR", false),
        apiCaseExecuteResult("api_case_execute_result", "apiCaseExecuteResult", "VARCHAR", false),
        apiCaseExecuteReportId("api_case_execute_report_id", "apiCaseExecuteReportId", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        testPlanName("test_plan_name", "testPlanName", "VARCHAR", false),
        apiCaseBugCount("api_case_bug_count", "apiCaseBugCount", "BIGINT", false);

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