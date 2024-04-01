package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiScenarioStep implements Serializable {
    @Schema(description = "步骤id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_step.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_step.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "场景id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_step.scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_step.scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String scenarioId;

    @Schema(description = "步骤名称")
    private String name;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_step.sort.not_blank}", groups = {Created.class})
    private Long sort;

    @Schema(description = "启用/禁用")
    private Boolean enable;

    @Schema(description = "资源id")
    private String resourceId;

    @Schema(description = "资源编号")
    private String resourceNum;

    @Schema(description = "步骤类型/API/CASE等")
    private String stepType;

    @Schema(description = "项目fk")
    private String projectId;

    @Schema(description = "父级fk")
    private String parentId;

    @Schema(description = "版本号")
    private String versionId;

    @Schema(description = "引用/复制/自定义")
    private String refType;

    @Schema(description = "循环等组件基础数据")
    private String config;

    @Schema(description = "记录跨项目复制的步骤的原项目ID")
    private String originProjectId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        scenarioId("scenario_id", "scenarioId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        sort("sort", "sort", "BIGINT", false),
        enable("enable", "enable", "BIT", true),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        resourceNum("resource_num", "resourceNum", "VARCHAR", false),
        stepType("step_type", "stepType", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        parentId("parent_id", "parentId", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        refType("ref_type", "refType", "VARCHAR", false),
        config("config", "config", "VARCHAR", false),
        originProjectId("origin_project_id", "originProjectId", "VARCHAR", false);

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