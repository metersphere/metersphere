package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@Data
public class FileMetadata implements Serializable {
    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{file_metadata.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{file_metadata.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "文件类型")
    private String type;

    @Schema(description = "文件大小", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_metadata.size.not_blank}", groups = {Created.class})
    private Long size;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "文件存储方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.storage.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.storage.length_range}", groups = {Created.class, Updated.class})
    private String storage;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "标签")
    private java.util.List<String> tags;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "文件所属模块")
    private String moduleId;

    @Schema(description = "文件存储路径")
    private String path;

    @Schema(description = "是否是最新版", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_metadata.latest.not_blank}", groups = {Created.class})
    private Boolean latest;

    @Schema(description = "启用/禁用;启用禁用（一般常用于jar文件）")
    private Boolean enable;

    @Schema(description = "同版本数据关联的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(description = "文件版本号")
    private String fileVersion;

    @Schema(description = "原始名（含后缀）")
    private String originalName;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        size("size", "size", "BIGINT", true),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        storage("storage", "storage", "VARCHAR", true),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        tags("tags", "tags", "VARCHAR", false),
        description("description", "description", "VARCHAR", false),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        path("path", "path", "VARCHAR", true),
        latest("latest", "latest", "BIT", false),
        enable("enable", "enable", "BIT", true),
        refId("ref_id", "refId", "VARCHAR", false),
        fileVersion("file_version", "fileVersion", "VARCHAR", false),
        originalName("original_name", "originalName", "VARCHAR", false);

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