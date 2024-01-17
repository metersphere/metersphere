package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiReportStep implements Serializable {
    @Schema(description = "步骤id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report_step.step_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report_step.step_id.length_range}", groups = {Created.class, Updated.class})
    private String stepId;

    @Schema(description = "报告id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report_step.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report_step.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(description = "步骤名称")
    private String name;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report_step.sort.not_blank}", groups = {Created.class})
    private Long sort;

    @Schema(description = "步骤类型/API/CASE等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report_step.step_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report_step.step_type.length_range}", groups = {Created.class, Updated.class})
    private String stepType;

    private static final long serialVersionUID = 1L;

    public enum Column {
        stepId("step_id", "stepId", "VARCHAR", false),
        reportId("report_id", "reportId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        sort("sort", "sort", "BIGINT", false),
        stepType("step_type", "stepType", "VARCHAR", false);

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