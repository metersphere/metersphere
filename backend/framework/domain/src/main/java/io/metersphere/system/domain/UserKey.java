package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UserKey implements Serializable {
    @Schema(description = "user_key ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_key.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_key.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "用户ID")
    private String createUser;

    @Schema(description = "access_key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_key.access_key.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_key.access_key.length_range}", groups = {Created.class, Updated.class})
    private String accessKey;

    @Schema(description = "secret key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_key.secret_key.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_key.secret_key.length_range}", groups = {Created.class, Updated.class})
    private String secretKey;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "状态")
    private Boolean enable;

    @Schema(description = "是否永久有效", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user_key.forever.not_blank}", groups = {Created.class})
    private Boolean forever;

    @Schema(description = "到期时间")
    private Long expireTime;

    @Schema(description = "描述")
    private String description;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        accessKey("access_key", "accessKey", "VARCHAR", false),
        secretKey("secret_key", "secretKey", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        enable("enable", "enable", "BIT", true),
        forever("forever", "forever", "BIT", false),
        expireTime("expire_time", "expireTime", "BIGINT", false),
        description("description", "description", "VARCHAR", false);

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