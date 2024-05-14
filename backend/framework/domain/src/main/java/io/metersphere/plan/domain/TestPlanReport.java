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
public class TestPlanReport implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "触发类型")
    private String triggerMode;

    @Schema(description = "执行状态;未执行, 执行中, 已停止, 已完成;", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.exec_status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report.exec_status.length_range}", groups = {Created.class, Updated.class})
    private String execStatus;

    @Schema(description = "结果状态;成功, 失败, 阻塞, 误报")
    private String resultStatus;

    @Schema(description = "通过阈值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.pass_threshold.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{test_plan_report.pass_threshold.length_range}", groups = {Created.class, Updated.class})
    private String passThreshold;

    @Schema(description = "通过率")
    private Long passRate;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        testPlanId("test_plan_id", "testPlanId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        createUser("create_user", "createUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        startTime("start_time", "startTime", "BIGINT", false),
        endTime("end_time", "endTime", "BIGINT", false),
        triggerMode("trigger_mode", "triggerMode", "VARCHAR", false),
        execStatus("exec_status", "execStatus", "VARCHAR", false),
        resultStatus("result_status", "resultStatus", "VARCHAR", false),
        passThreshold("pass_threshold", "passThreshold", "VARCHAR", false),
        passRate("pass_rate", "passRate", "DECIMAL", false);

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