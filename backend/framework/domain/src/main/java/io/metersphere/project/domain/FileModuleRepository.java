package io.metersphere.project.domain;

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
public class FileModuleRepository implements Serializable {
    @Schema(description = "file_module_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module_repository.file_module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_module_repository.file_module_id.length_range}", groups = {Created.class, Updated.class})
    private String fileModuleId;

    @Schema(description = "所属平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module_repository.platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{file_module_repository.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(description = "存储库地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module_repository.url.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{file_module_repository.url.length_range}", groups = {Created.class, Updated.class})
    private String url;

    @Schema(description = "存储库Token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module_repository.token.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{file_module_repository.token.length_range}", groups = {Created.class, Updated.class})
    private String token;

    @Schema(description = "用户名;platform为Gitee时必填")
    private String userName;

    private static final long serialVersionUID = 1L;

    public enum Column {
        fileModuleId("file_module_id", "fileModuleId", "VARCHAR", false),
        platform("platform", "platform", "VARCHAR", false),
        url("url", "url", "VARCHAR", false),
        token("token", "token", "VARCHAR", false),
        userName("user_name", "userName", "VARCHAR", false);

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