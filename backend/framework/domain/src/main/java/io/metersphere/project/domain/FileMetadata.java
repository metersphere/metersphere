package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FileMetadata implements Serializable {
    @Schema(title = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{file_metadata.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{file_metadata.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "文件类型")
    private String type;

    @Schema(title = "文件大小", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_metadata.size.not_blank}", groups = {Created.class})
    private Long size;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "文件存储方式")
    private String storage;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "修改人")
    private String updateUser;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "文件所属模块")
    private String moduleId;

    @Schema(title = "是否加载jar（开启后用于接口测试执行时使用）")
    private Boolean loadJar;

    @Schema(title = "文件存储路径")
    private String path;

    @Schema(title = "资源作用范围，主要兼容2.1版本前的历史数据，后续版本不再产生数据")
    private String resourceType;

    @Schema(title = "是否是最新版")
    private Boolean latest;

    @Schema(title = "同版本数据关联的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

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
        loadJar("load_jar", "loadJar", "BIT", false),
        path("path", "path", "VARCHAR", true),
        resourceType("resource_type", "resourceType", "VARCHAR", false),
        latest("latest", "latest", "BIT", false),
        refId("ref_id", "refId", "VARCHAR", false);

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