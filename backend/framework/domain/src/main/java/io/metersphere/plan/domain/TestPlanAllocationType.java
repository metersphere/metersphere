package io.metersphere.plan.domain;

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
public class TestPlanAllocationType implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation_type.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_allocation_type.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation_type.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_allocation_type.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description = "测试集类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation_type.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan_allocation_type.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "测试集类型(功能/接口/场景)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation_type.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_allocation_type.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "执行方式(串行/并行)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation_type.execute_method.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_allocation_type.execute_method.length_range}", groups = {Created.class, Updated.class})
    private String executeMethod;

    @Schema(description = "自定义排序，间隔为2的N次幂", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_allocation_type.pos.not_blank}", groups = {Created.class})
    private Long pos;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        testPlanId("test_plan_id", "testPlanId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        executeMethod("execute_method", "executeMethod", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false);

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