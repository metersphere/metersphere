package io.metersphere.functional.domain;

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
public class ExportTask implements Serializable {
    @Schema(description = "任务唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{export_task.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{export_task.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{export_task.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{export_task.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "文件id")
    private String fileId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{export_task.state.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{export_task.state.length_range}", groups = {Created.class, Updated.class})
    private String state;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String updateUser;

    @Schema(description = "创建时间")
    private Long updateTime;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{export_task.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{export_task.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "文件类型")
    private String fileType;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        fileId("file_id", "fileId", "VARCHAR", false),
        state("state", "state", "VARCHAR", true),
        createUser("create_user", "createUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        fileType("file_type", "fileType", "VARCHAR", false);

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