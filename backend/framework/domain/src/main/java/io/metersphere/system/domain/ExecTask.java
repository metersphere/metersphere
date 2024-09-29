package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ExecTask implements Serializable {
    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{exec_task.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long num;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task.task_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{exec_task.task_name.length_range}", groups = {Created.class, Updated.class})
    private String taskName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{exec_task.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "用例数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task.case_count.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 19, message = "{exec_task.case_count.length_range}", groups = {Created.class, Updated.class})
    private Long caseCount;

    @Schema(description = "执行结果")
    private String result;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task.task_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task.task_type.length_range}", groups = {Created.class, Updated.class})
    private String taskType;

    @Schema(description = "执行模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task.trigger_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{exec_task.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        num("num", "num", "BIGINT", false),
        taskName("task_name", "taskName", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        caseCount("case_count", "caseCount", "BIGINT", false),
        result("result", "result", "VARCHAR", true),
        taskType("task_type", "taskType", "VARCHAR", false),
        triggerMode("trigger_mode", "triggerMode", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        organizationId("organization_id", "organizationId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        startTime("start_time", "startTime", "BIGINT", false),
        endTime("end_time", "endTime", "BIGINT", false);

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