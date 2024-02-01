package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FileAssociation implements Serializable {
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{file_association.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.source_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.source_type.length_range}", groups = {Created.class, Updated.class})
    private String sourceType;

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(description = "文件同版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.file_ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.file_ref_id.length_range}", groups = {Created.class, Updated.class})
    private String fileRefId;

    @Schema(description = "文件版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.file_version.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.file_version.length_range}", groups = {Created.class, Updated.class})
    private String fileVersion;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_association.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "删除时的文件名称")
    private String deletedFileName;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        sourceType("source_type", "sourceType", "VARCHAR", false),
        sourceId("source_id", "sourceId", "VARCHAR", false),
        fileId("file_id", "fileId", "VARCHAR", false),
        fileRefId("file_ref_id", "fileRefId", "VARCHAR", false),
        fileVersion("file_version", "fileVersion", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        deletedFileName("deleted_file_name", "deletedFileName", "VARCHAR", false);

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