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
public class UserViewCondition implements Serializable {
    @Schema(description = "条件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view_condition.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_view_condition.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "视图ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view_condition.user_view_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_view_condition.user_view_id.length_range}", groups = {Created.class, Updated.class})
    private String userViewId;

    @Schema(description = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view_condition.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user_view_condition.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "查询的期望值")
    private String value;

    @Schema(description = "期望值的数据类型：STRING,INT,FLOAT,ARRAY, BOOLEAN")
    private String valueType;

    @Schema(description = "是否为自定义字段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user_view_condition.custom_field.not_blank}", groups = {Created.class})
    private Boolean customField;

    @Schema(description = "操作符：等于、大于、小于、等")
    private String operator;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        userViewId("user_view_id", "userViewId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        value("value", "value", "VARCHAR", true),
        valueType("value_type", "valueType", "VARCHAR", false),
        customField("custom_field", "customField", "BIT", false),
        operator("operator", "operator", "VARCHAR", true);

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