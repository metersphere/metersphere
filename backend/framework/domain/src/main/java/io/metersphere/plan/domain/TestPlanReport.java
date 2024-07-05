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

    @Schema(description = "开始时间;计划开始执行的时间")
    private Long startTime;

    @Schema(description = "结束时间;计划结束执行的时间")
    private Long endTime;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.exec_status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report.exec_status.length_range}", groups = {Created.class, Updated.class})
    private String execStatus;

    @Schema(description = "结果状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.result_status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report.result_status.length_range}", groups = {Created.class, Updated.class})
    private String resultStatus;

    @Schema(description = "通过率")
    private Double passRate;

    @Schema(description = "触发类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.trigger_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(description = "通过阈值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report.pass_threshold.not_blank}", groups = {Created.class})
    private Double passThreshold;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "是否是集成报告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report.integrated.not_blank}", groups = {Created.class})
    private Boolean integrated;

    @Schema(description = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "执行率")
    private Double executeRate;

    @Schema(description = "独立报告的父级ID")
    private String parentId;

    @Schema(description = "测试计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_report.test_plan_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan_report.test_plan_name.length_range}", groups = {Created.class, Updated.class})
    private String testPlanName;

    @Schema(description = "是否默认布局", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_report.default_layout.not_blank}", groups = {Created.class})
    private Boolean defaultLayout;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        testPlanId("test_plan_id", "testPlanId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        createUser("create_user", "createUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        startTime("start_time", "startTime", "BIGINT", false),
        endTime("end_time", "endTime", "BIGINT", false),
        execStatus("exec_status", "execStatus", "VARCHAR", false),
        resultStatus("result_status", "resultStatus", "VARCHAR", false),
        passRate("pass_rate", "passRate", "DECIMAL", false),
        triggerMode("trigger_mode", "triggerMode", "VARCHAR", false),
        passThreshold("pass_threshold", "passThreshold", "DECIMAL", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        integrated("integrated", "integrated", "BIT", false),
        deleted("deleted", "deleted", "BIT", false),
        executeRate("execute_rate", "executeRate", "DECIMAL", false),
        parentId("parent_id", "parentId", "VARCHAR", false),
        testPlanName("test_plan_name", "testPlanName", "VARCHAR", false),
        defaultLayout("default_layout", "defaultLayout", "BIT", false);

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