package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@Data
public class ApiDefinitionMock implements Serializable {
    @Schema(description = "mock pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "mock名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_definition_mock.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "自定义标签")
    private java.util.List<String> tags;

    @Schema(description = "启用/禁用")
    private Boolean enable;

    @Schema(description = "mock编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.expect_num.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock.expect_num.length_range}", groups = {Created.class, Updated.class})
    private String expectNum;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.api_definition_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    private String apiDefinitionId;

    @Schema(description = "")
    private Integer statusCode;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "版本id")
    private String versionId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        tags("tags", "tags", "VARCHAR", false),
        enable("enable", "enable", "BIT", true),
        expectNum("expect_num", "expectNum", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        apiDefinitionId("api_definition_id", "apiDefinitionId", "VARCHAR", false),
        statusCode("status_code", "statusCode", "INTEGER", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false);

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