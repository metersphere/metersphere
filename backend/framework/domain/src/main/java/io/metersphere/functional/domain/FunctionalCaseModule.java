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
public class FunctionalCaseModule implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{functional_case_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "父节点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(description =  "节点的层级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.level.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{functional_case_module.level.length_range}", groups = {Created.class, Updated.class})
    private Integer level;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "同一节点下的顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_module.pos.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 19, message = "{functional_case_module.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(description =  "创建人")
    private String createUser;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        parentId("parent_id", "parentId", "VARCHAR", false),
        level("level", "level", "INTEGER", true),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        pos("pos", "pos", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false);

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