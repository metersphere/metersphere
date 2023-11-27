package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UserLocalConfig implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_local_config.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_local_config.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "本地执行程序url", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_local_config.user_url.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_local_config.user_url.length_range}", groups = {Created.class, Updated.class})
    private String userUrl;

    @Schema(description = "本地执行优先")
    private Boolean enable;

    @Schema(description = "API/UI", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_local_config.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_local_config.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "创建人")
    private String createUser;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        userUrl("user_url", "userUrl", "VARCHAR", false),
        enable("enable", "enable", "BIT", true),
        type("type", "type", "VARCHAR", true),
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