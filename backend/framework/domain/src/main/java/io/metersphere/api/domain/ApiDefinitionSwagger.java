package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiDefinitionSwagger implements Serializable {
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_swagger.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_swagger.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "url地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_swagger.swagger_url.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 500, message = "{api_definition_swagger.swagger_url.length_range}", groups = {Created.class, Updated.class})
    private String swaggerUrl;

    @Schema(description = "模块fk")
    private String moduleId;

    @Schema(description = "模块路径")
    private String modulePath;

    @Schema(description = "导入模式/覆盖/不覆盖")
    private Boolean mode;

    @Schema(description = "项目fk")
    private String projectId;

    @Schema(description = "导入版本")
    private String versionId;

    @Schema(description = "鉴权配置信息")
    private byte[] config;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        swaggerUrl("swagger_url", "swaggerUrl", "VARCHAR", false),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        modulePath("module_path", "modulePath", "VARCHAR", false),
        mode("mode", "mode", "BIT", true),
        projectId("project_id", "projectId", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        config("config", "config", "LONGVARBINARY", false);

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