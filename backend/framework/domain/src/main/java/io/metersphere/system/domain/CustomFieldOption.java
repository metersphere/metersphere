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
public class CustomFieldOption implements Serializable {
    @Schema(description = "自定义字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_option.field_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_option.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(description = "选项值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_option.value.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_option.value.length_range}", groups = {Created.class, Updated.class})
    private String value;

    @Schema(description = "选项值名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_option.text.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{custom_field_option.text.length_range}", groups = {Created.class, Updated.class})
    private String text;

    @Schema(description = "是否内置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{custom_field_option.internal.not_blank}", groups = {Created.class})
    private Boolean internal;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{custom_field_option.pos.not_blank}", groups = {Created.class})
    private Integer pos;

    private static final long serialVersionUID = 1L;

    public enum Column {
        fieldId("field_id", "fieldId", "VARCHAR", false),
        value("value", "value", "VARCHAR", true),
        text("text", "text", "VARCHAR", true),
        internal("internal", "internal", "BIT", false),
        pos("pos", "pos", "INTEGER", false);

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