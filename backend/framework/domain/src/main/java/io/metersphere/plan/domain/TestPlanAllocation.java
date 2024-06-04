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
public class TestPlanAllocation implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_allocation.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_allocation.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation.test_resource_pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_allocation.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String testResourcePoolId;

    @Schema(description = "是否失败重试", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_allocation.retry_on_fail.not_blank}", groups = {Created.class})
    private Boolean retryOnFail;

    @Schema(description = "失败重试类型(步骤/场景)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_allocation.retry_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_allocation.retry_type.length_range}", groups = {Created.class, Updated.class})
    private String retryType;

    @Schema(description = "失败重试次数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_allocation.retry_times.not_blank}", groups = {Created.class})
    private Integer retryTimes;

    @Schema(description = "失败重试间隔(单位: ms)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_allocation.retry_interval.not_blank}", groups = {Created.class})
    private Integer retryInterval;

    @Schema(description = "是否失败停止", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_allocation.stop_on_fail.not_blank}", groups = {Created.class})
    private Boolean stopOnFail;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        testPlanId("test_plan_id", "testPlanId", "VARCHAR", false),
        testResourcePoolId("test_resource_pool_id", "testResourcePoolId", "VARCHAR", false),
        retryOnFail("retry_on_fail", "retryOnFail", "BIT", false),
        retryType("retry_type", "retryType", "VARCHAR", false),
        retryTimes("retry_times", "retryTimes", "INTEGER", false),
        retryInterval("retry_interval", "retryInterval", "INTEGER", false),
        stopOnFail("stop_on_fail", "stopOnFail", "BIT", false);

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