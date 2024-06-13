package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class User implements Serializable {
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.email.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{user.email.length_range}", groups = {Created.class, Updated.class})
    private String email;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "当前组织ID")
    private String lastOrganizationId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "来源：LOCAL OIDC CAS OAUTH2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.source.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user.source.length_range}", groups = {Created.class, Updated.class})
    private String source;

    @Schema(description = "当前项目ID")
    private String lastProjectId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "身份令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.cft_token.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user.cft_token.length_range}", groups = {Created.class, Updated.class})
    private String cftToken;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        email("email", "email", "VARCHAR", false),
        password("password", "password", "VARCHAR", true),
        enable("enable", "enable", "BIT", true),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        language("language", "language", "VARCHAR", true),
        lastOrganizationId("last_organization_id", "lastOrganizationId", "VARCHAR", false),
        phone("phone", "phone", "VARCHAR", false),
        source("source", "source", "VARCHAR", true),
        lastProjectId("last_project_id", "lastProjectId", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        cftToken("cft_token", "cftToken", "VARCHAR", false);

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