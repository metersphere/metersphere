package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UiCustomVariable implements Serializable {
    @Schema(description =  "指令ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_variable.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_variable.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description =  "变量类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_variable.type.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 100, message = "{ui_custom_variable.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description =  "变量值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_variable.value.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 1000, message = "{ui_custom_variable.value.length_range}", groups = {Created.class, Updated.class})
    private String value;

    @Schema(description =  "变量名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_variable.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{ui_custom_variable.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "描述")
    private String description;

    @Schema(description =  "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_custom_variable.deleted.not_blank}", groups = {Created.class, Updated.class})
    private Boolean deleted;

    @Schema(description =  "是否是出参", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_custom_variable.out_put.not_blank}", groups = {Created.class, Updated.class})
    private Boolean outPut;

    @Schema(description =  "启用禁用")
    @NotNull(message = "{ui_custom_variable.enable.not_blank}", groups = {Created.class, Updated.class})
    private Boolean enable;

    private static final long serialVersionUID = 1L;

    public enum Column {
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        type("type", "type", "VARCHAR", true),
        value("value", "value", "VARCHAR", true),
        name("name", "name", "VARCHAR", true),
        description("description", "description", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        outPut("out_put", "outPut", "BIT", false),
        enable("enable", "enable", "BIT", true);

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