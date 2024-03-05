package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class Notification implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.id.not_blank}", groups = {Updated.class})
    private Long id;

    @Schema(description = "通知类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{notification.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "接收人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.receiver.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.receiver.length_range}", groups = {Created.class, Updated.class})
    private String receiver;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.subject.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{notification.subject.length_range}", groups = {Created.class, Updated.class})
    private String subject;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{notification.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "操作人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.operator.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.operator.length_range}", groups = {Created.class, Updated.class})
    private String operator;

    @Schema(description = "操作", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.operation.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.operation.length_range}", groups = {Created.class, Updated.class})
    private String operation;

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "组织id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{notification.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.resource_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{notification.resource_type.length_range}", groups = {Created.class, Updated.class})
    private String resourceType;

    @Schema(description = "资源名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.resource_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{notification.resource_name.length_range}", groups = {Created.class, Updated.class})
    private String resourceName;

    @Schema(description = "通知内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{notification.content.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 65535, message = "{notification.content.length_range}", groups = {Created.class, Updated.class})
    private String content;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "BIGINT", false),
        type("type", "type", "VARCHAR", true),
        receiver("receiver", "receiver", "VARCHAR", false),
        subject("subject", "subject", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        createTime("create_time", "createTime", "BIGINT", false),
        operator("operator", "operator", "VARCHAR", true),
        operation("operation", "operation", "VARCHAR", true),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        organizationId("organization_id", "organizationId", "VARCHAR", false),
        resourceType("resource_type", "resourceType", "VARCHAR", false),
        resourceName("resource_name", "resourceName", "VARCHAR", false),
        content("content", "content", "LONGVARCHAR", false);

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