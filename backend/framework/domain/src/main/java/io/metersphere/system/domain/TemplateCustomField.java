package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class TemplateCustomField implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template_custom_field.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{template_custom_field.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template_custom_field.field_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template_custom_field.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(description = "模版ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template_custom_field.template_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template_custom_field.template_id.length_range}", groups = {Created.class, Updated.class})
    private String templateId;

    @Schema(description = "是否必填", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{template_custom_field.required.not_blank}", groups = {Created.class})
    private Boolean required;

    @Schema(description = "排序字段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{template_custom_field.pos.not_blank}", groups = {Created.class})
    private Integer pos;

    @Schema(description = "api字段名")
    private String apiFieldId;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{template_custom_field.system_field.not_blank}", groups = {Created.class})
    private Boolean systemField;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        fieldId("field_id", "fieldId", "VARCHAR", false),
        templateId("template_id", "templateId", "VARCHAR", false),
        required("required", "required", "BIT", false),
        pos("pos", "pos", "INTEGER", false),
        apiFieldId("api_field_id", "apiFieldId", "VARCHAR", false),
        defaultValue("default_value", "defaultValue", "VARCHAR", false),
        systemField("system_field", "systemField", "BIT", false);

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