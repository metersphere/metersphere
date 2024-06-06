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
public class TestPlanConfig implements Serializable {
    @Schema(description = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_config.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_config.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description = "是否自定更新功能用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_config.automatic_status_update.not_blank}", groups = {Created.class})
    private Boolean automaticStatusUpdate;

    @Schema(description = "是否允许重复添加用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_config.repeat_case.not_blank}", groups = {Created.class})
    private Boolean repeatCase;

    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_config.pass_threshold.not_blank}", groups = {Created.class})
    private Double passThreshold;

    @Schema(description = "不同用例之间的执行方式(串行/并行)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_config.case_run_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_config.case_run_mode.length_range}", groups = {Created.class, Updated.class})
    private String caseRunMode;

    private static final long serialVersionUID = 1L;

    public enum Column {
        testPlanId("test_plan_id", "testPlanId", "VARCHAR", false),
        automaticStatusUpdate("automatic_status_update", "automaticStatusUpdate", "BIT", false),
        repeatCase("repeat_case", "repeatCase", "BIT", false),
        passThreshold("pass_threshold", "passThreshold", "DECIMAL", false),
        caseRunMode("case_run_mode", "caseRunMode", "VARCHAR", false);

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