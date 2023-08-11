package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class EnvironmentGroupRelation implements Serializable {
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group_relation.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment_group_relation.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "环境组id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group_relation.environment_group_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{environment_group_relation.environment_group_id.length_range}", groups = {Created.class, Updated.class})
    private String environmentGroupId;

    @Schema(description = "环境ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group_relation.environment_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{environment_group_relation.environment_id.length_range}", groups = {Created.class, Updated.class})
    private String environmentId;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group_relation.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{environment_group_relation.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        environmentGroupId("environment_group_id", "environmentGroupId", "VARCHAR", false),
        environmentId("environment_id", "environmentId", "VARCHAR", false),
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