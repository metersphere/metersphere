package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UserRoleRelation implements Serializable {
    @Schema(description = "用户组关系ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    @Schema(description = "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.role_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.role_id.length_range}", groups = {Created.class, Updated.class})
    private String roleId;

    @Schema(description = "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(description = "记录所在的组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        userId("user_id", "userId", "VARCHAR", false),
        roleId("role_id", "roleId", "VARCHAR", false),
        sourceId("source_id", "sourceId", "VARCHAR", false),
        organizationId("organization_id", "organizationId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
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