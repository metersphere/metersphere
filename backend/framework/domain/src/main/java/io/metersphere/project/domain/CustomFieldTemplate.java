package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class CustomFieldTemplate implements Serializable {
    @Schema(title = "自定义模版ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_template.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{custom_field_template.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "自定义字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_template.field_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_template.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(title = "模版ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_template.template_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_template.template_id.length_range}", groups = {Created.class, Updated.class})
    private String templateId;

    @Schema(title = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_template.scene.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{custom_field_template.scene.length_range}", groups = {Created.class, Updated.class})
    private String scene;

    @Schema(title = "是否必填")
    private Boolean required;

    @Schema(title = "排序字段")
    private Integer pos;

    @Schema(title = "自定义数据")
    private String customData;

    @Schema(title = "自定义表头")
    private String key;

    @Schema(title = "默认值")
    private byte[] defaultValue;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        fieldId("field_id", "fieldId", "VARCHAR", false),
        templateId("template_id", "templateId", "VARCHAR", false),
        scene("scene", "scene", "VARCHAR", false),
        required("required", "required", "BIT", false),
        pos("pos", "pos", "INTEGER", false),
        customData("custom_data", "customData", "VARCHAR", false),
        key("key", "key", "VARCHAR", true),
        defaultValue("default_value", "defaultValue", "LONGVARBINARY", false);

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