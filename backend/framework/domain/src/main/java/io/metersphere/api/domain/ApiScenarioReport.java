package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiScenarioReport implements Serializable {
    @Schema(description = "场景报告pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_scenario_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "删除人")
    private String deleteUser;

    @Schema(description = "删除标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "通过率")
    private Long passRate;

    @Schema(description = "报告状态/SUCCESS/ERROR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "触发方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.trigger_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(description = "执行模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.run_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    private String runMode;

    @Schema(description = "资源池", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    private String poolId;

    @Schema(description = "版本fk")
    private String versionId;

    @Schema(description = "是否是集成报告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.integrated.not_blank}", groups = {Created.class})
    private Boolean integrated;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "场景fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String scenarioId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        passRate("pass_rate", "passRate", "BIGINT", false),
        status("status", "status", "VARCHAR", true),
        triggerMode("trigger_mode", "triggerMode", "VARCHAR", false),
        runMode("run_mode", "runMode", "VARCHAR", false),
        poolId("pool_id", "poolId", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        integrated("integrated", "integrated", "BIT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        scenarioId("scenario_id", "scenarioId", "VARCHAR", false);

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