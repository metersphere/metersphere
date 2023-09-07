package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class MessageTask implements Serializable {
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{message_task.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "通知事件类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.event.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{message_task.event.length_range}", groups = {Created.class, Updated.class})
    private String event;

    @Schema(description = "接收人id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.receiver.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{message_task.receiver.length_range}", groups = {Created.class, Updated.class})
    private String receiver;

    @Schema(description = "机器人id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.project_robot_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{message_task.project_robot_id.length_range}", groups = {Created.class, Updated.class})
    private String projectRobotId;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.task_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{message_task.task_type.length_range}", groups = {Created.class, Updated.class})
    private String taskType;

    @Schema(description = "具体测试的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.test_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{message_task.test_id.length_range}", groups = {Created.class, Updated.class})
    private String testId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{message_task.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "是否启用")
    @NotNull(message = "{message_task.enable.not_blank}", groups = {Created.class, Updated.class})
    private Boolean enable;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        event("event", "event", "VARCHAR", false),
        receiver("receiver", "receiver", "VARCHAR", false),
        projectRobotId("project_robot_id", "projectRobotId", "VARCHAR", false),
        taskType("task_type", "taskType", "VARCHAR", false),
        testId("test_id", "testId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        enable("enable", "enable", "BIT", true);

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