package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiFileResource implements Serializable {
    @Schema(description = "资源ID(接口用例等)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_file_resource.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_file_resource.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_file_resource.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_file_resource.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_file_resource.file_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_file_resource.file_name.length_range}", groups = {Created.class, Updated.class})
    private String fileName;

    @Schema(description = "资源类型(API_DEBUG,API,API_CASE,API_SCENARIO)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_file_resource.resource_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_file_resource.resource_type.length_range}", groups = {Created.class, Updated.class})
    private String resourceType;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_file_resource.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_file_resource.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        fileId("file_id", "fileId", "VARCHAR", false),
        fileName("file_name", "fileName", "VARCHAR", false),
        resourceType("resource_type", "resourceType", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false);

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