package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

@Data
public class AiUserPromptConfig implements Serializable {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_ai_prompt_config.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_ai_prompt_config.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_ai_prompt_config.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_ai_prompt_config.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    @Schema(description = "配置类型（API/CASE/BUG）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_ai_prompt_config.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_ai_prompt_config.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "配置内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user_ai_prompt_config.config.not_blank}", groups = {Created.class})
    private byte[] config;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        userId("user_id", "userId", "VARCHAR", false),
        type("type", "type", "VARCHAR", true),
        config("config", "config", "LONGVARBINARY", false);

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