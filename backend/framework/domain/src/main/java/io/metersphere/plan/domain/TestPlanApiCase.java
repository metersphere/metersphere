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
public class TestPlanApiCase implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_api_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_api_case.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description = "接口用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.api_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_api_case.api_case_id.length_range}", groups = {Created.class, Updated.class})
    private String apiCaseId;

    @Schema(description = "最后执行结果")
    private String lastExecResult;

    @Schema(description = "最后执行报告")
    private String lastExecReportId;

    @Schema(description = "执行人")
    private String executeUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "自定义排序，间隔为2的n次幂", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_api_case.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "测试计划集id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.test_plan_collection_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_api_case.test_plan_collection_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanCollectionId;

    @Schema(description = "最后执行时间")
    private Long lastExecTime;

    @Schema(description = "所属环境")
    private String environmentId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        testPlanId("test_plan_id", "testPlanId", "VARCHAR", false),
        apiCaseId("api_case_id", "apiCaseId", "VARCHAR", false),
        lastExecResult("last_exec_result", "lastExecResult", "VARCHAR", false),
        lastExecReportId("last_exec_report_id", "lastExecReportId", "VARCHAR", false),
        executeUser("execute_user", "executeUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        testPlanCollectionId("test_plan_collection_id", "testPlanCollectionId", "VARCHAR", false),
        lastExecTime("last_exec_time", "lastExecTime", "BIGINT", false),
        environmentId("environment_id", "environmentId", "LONGVARCHAR", false);

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