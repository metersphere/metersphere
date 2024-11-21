package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ExecTaskItem implements Serializable {
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{exec_task_item.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.task_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task_item.task_id.length_range}", groups = {Created.class, Updated.class})
    private String taskId;

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task_item.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description = "资源名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.resource_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{exec_task_item.resource_name.length_range}", groups = {Created.class, Updated.class})
    private String resourceName;

    @Schema(description = "任务来源（任务组下的任务id）")
    private String taskOrigin;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{exec_task_item.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "执行结果")
    private String result;

    @Schema(description = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.resource_pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task_item.resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String resourcePoolId;

    @Schema(description = "节点")
    private String resourcePoolNode;

    @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.resource_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task_item.resource_type.length_range}", groups = {Created.class, Updated.class})
    private String resourceType;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task_item.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task_item.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description = "线程ID")
    private String threadId;

    @Schema(description = "执行开始时间")
    private Long startTime;

    @Schema(description = "执行完成时间")
    private Long endTime;

    @Schema(description = "执行人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{exec_task_item.executor.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{exec_task_item.executor.length_range}", groups = {Created.class, Updated.class})
    private String executor;

    @Schema(description = "测试集ID")
    private String collectionId;

    @Schema(description = "删除标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{exec_task_item.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "用例表id")
    private String caseId;

    @Schema(description = "异常信息")
    private String errorMessage;

    @Schema(description = "是否是重跑任务项")
    private Boolean rerun;

    @Schema(description = "创建时间")
    private Long createTime;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        taskId("task_id", "taskId", "VARCHAR", false),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        resourceName("resource_name", "resourceName", "VARCHAR", false),
        taskOrigin("task_origin", "taskOrigin", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        result("result", "result", "VARCHAR", true),
        resourcePoolId("resource_pool_id", "resourcePoolId", "VARCHAR", false),
        resourcePoolNode("resource_pool_node", "resourcePoolNode", "VARCHAR", false),
        resourceType("resource_type", "resourceType", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        organizationId("organization_id", "organizationId", "VARCHAR", false),
        threadId("thread_id", "threadId", "VARCHAR", false),
        startTime("start_time", "startTime", "BIGINT", false),
        endTime("end_time", "endTime", "BIGINT", false),
        executor("executor", "executor", "VARCHAR", false),
        collectionId("collection_id", "collectionId", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        caseId("case_id", "caseId", "VARCHAR", false),
        errorMessage("error_message", "errorMessage", "VARCHAR", false),
        rerun("rerun", "rerun", "BIT", false),
        createTime("create_time", "createTime", "BIGINT", false);

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