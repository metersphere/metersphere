package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class PlatformSource implements Serializable {
    @Schema(description = "平台名称（国际飞书:LARK_SUITE，飞书:LARK，钉钉:DING_TALK，企业微信:WE_COM）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{platform_source.platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{platform_source.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(description = "是否开启")
    private Boolean enable;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{platform_source.valid.not_blank}", groups = {Created.class})
    private Boolean valid;

    @Schema(description = "平台信息配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{platform_source.config.not_blank}", groups = {Created.class})
    private byte[] config;

    private static final long serialVersionUID = 1L;

    public enum Column {
        platform("platform", "platform", "VARCHAR", false),
        enable("enable", "enable", "BIT", true),
        valid("valid", "valid", "BIT", true),
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