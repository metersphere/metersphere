package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UserInvite implements Serializable {
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_invite.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_invite.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "邀请邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_invite.email.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user_invite.email.length_range}", groups = {Created.class, Updated.class})
    private String email;

    @Schema(description = "邀请用户", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_invite.invite_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_invite.invite_user.length_range}", groups = {Created.class, Updated.class})
    private String inviteUser;

    @Schema(description = "邀请时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user_invite.invite_time.not_blank}", groups = {Created.class})
    private Long inviteTime;

    @Schema(description = "组织ID")
    private String organizationId;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "所属权限")
    private String roles;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        email("email", "email", "VARCHAR", false),
        inviteUser("invite_user", "inviteUser", "VARCHAR", false),
        inviteTime("invite_time", "inviteTime", "BIGINT", false),
        organizationId("organization_id", "organizationId", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        roles("roles", "roles", "LONGVARCHAR", false);

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