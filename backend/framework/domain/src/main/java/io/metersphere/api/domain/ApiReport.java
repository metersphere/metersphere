package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiReport implements Serializable {
    @Schema(description = "接口结果报告pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "接口报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 200, message = "{api_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "资源fk/api_definition_id/api_test_case_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "创建人fk")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "结果状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "接口开始执行时间")
    private Long startTime;

    @Schema(description = "接口执行结束时间")
    private Long endTime;

    @Schema(description = "执行模块/API/CASE/API_PLAN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.run_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    private String runMode;

    @Schema(description = "资源池fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    private String poolId;

    @Schema(description = "触发模式/手动/批量/定时任务/JENKINS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.trigger_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(description = "版本fk")
    private String versionId;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "集成报告id/api_scenario_report_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.integrated_report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.integrated_report_id.length_range}", groups = {Created.class, Updated.class})
    private String integratedReportId;

    @Schema(description = "是否为集成报告，默认否", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.integrated.not_blank}", groups = {Created.class})
    private Boolean integrated;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        status("status", "status", "VARCHAR", true),
        startTime("start_time", "startTime", "BIGINT", false),
        endTime("end_time", "endTime", "BIGINT", false),
        runMode("run_mode", "runMode", "VARCHAR", false),
        poolId("pool_id", "poolId", "VARCHAR", false),
        triggerMode("trigger_mode", "triggerMode", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        integratedReportId("integrated_report_id", "integratedReportId", "VARCHAR", false),
        integrated("integrated", "integrated", "BIT", false);

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