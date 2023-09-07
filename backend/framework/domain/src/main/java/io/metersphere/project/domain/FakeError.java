package io.metersphere.project.domain;

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
public class FakeError implements Serializable {
    @Schema(description =  "误报ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{fake_error.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{fake_error.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "误报名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{fake_error.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "更新人")
    private String updateUser;

    @Schema(description = "匹配类型/文本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{fake_error.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "响应内容类型/header/data/body", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.resp_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{fake_error.resp_type.length_range}", groups = {Created.class, Updated.class})
    private String respType;

    @Schema(description = "操作类型/大于/等于/小于", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.relation.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{fake_error.relation.length_range}", groups = {Created.class, Updated.class})
    private String relation;

    @Schema(description = "表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.expression.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{fake_error.expression.length_range}", groups = {Created.class, Updated.class})
    private String expression;

    @Schema(description = "启用/禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.enable.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{fake_error.enable.length_range}", groups = {Created.class, Updated.class})
    private Boolean enable;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        type("type", "type", "VARCHAR", true),
        respType("resp_type", "respType", "VARCHAR", false),
        relation("relation", "relation", "VARCHAR", false),
        expression("expression", "expression", "VARCHAR", false),
        enable("enable", "enable", "BIT", true);

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