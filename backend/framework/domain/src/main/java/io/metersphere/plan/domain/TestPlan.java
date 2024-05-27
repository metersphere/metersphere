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
public class TestPlan implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "num")
    private Long num;

    @Schema(description = "测试计划所属项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "测试计划组ID;默认为none.只关联type为group的测试计划", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.group_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan.group_id.length_range}", groups = {Created.class, Updated.class})
    private String groupId;

    @Schema(description = "测试计划模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(description = "测试计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "测试计划状态;未开始，进行中，已完成，已归档", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{test_plan.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "数据类型;测试计划组（group）/测试计划（testPlan）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{test_plan.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "标签")
    private java.util.List<String> tags;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "计划开始时间")
    private Long plannedStartTime;

    @Schema(description = "计划结束时间")
    private Long plannedEndTime;

    @Schema(description = "实际开始时间")
    private Long actualStartTime;

    @Schema(description = "实际结束时间")
    private Long actualEndTime;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan.pos.not_blank}", groups = {Created.class})
    private Long pos;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        num("num", "num", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        groupId("group_id", "groupId", "VARCHAR", false),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        status("status", "status", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        tags("tags", "tags", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        plannedStartTime("planned_start_time", "plannedStartTime", "BIGINT", false),
        plannedEndTime("planned_end_time", "plannedEndTime", "BIGINT", false),
        actualStartTime("actual_start_time", "actualStartTime", "BIGINT", false),
        actualEndTime("actual_end_time", "actualEndTime", "BIGINT", false),
        description("description", "description", "VARCHAR", false),
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