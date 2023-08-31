package io.metersphere.sdk.domain;

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
public class OperationLog implements Serializable {
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.id.not_blank}", groups = {Updated.class})
    private Long id;

    @Schema(description =  "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operation_log.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "组织id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operation_log.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description =  "操作时间")
    private Long createTime;

    @Schema(description =  "操作人")
    private String createUser;

    @Schema(description =  "资源id")
    private String sourceId;

    @Schema(description = "操作方法", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.method.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{operation_log.method.length_range}", groups = {Created.class, Updated.class})
    private String method;

    @Schema(description =  "操作类型/add/update/delete", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{operation_log.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description =  "操作模块/api/case/scenario/ui")
    private String module;

    @Schema(description =  "操作详情")
    private String content;

    @Schema(description =  "操作路径")
    private String path;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        organizationId("organization_id", "organizationId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        sourceId("source_id", "sourceId", "VARCHAR", false),
        method("method", "method", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        module("module", "module", "VARCHAR", true),
        content("content", "content", "VARCHAR", false),
        path("path", "path", "VARCHAR", true);

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