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
    @Schema(description = "场景报告pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "场景资源fk/api_definition_id/api_test_case_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "删除人")
    private String deleteUser;

    @Schema(description = "删除标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "开始时间/同创建时间一致", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.start_time.not_blank}", groups = {Created.class})
    private Long startTime;

    @Schema(description = "结束时间/报告执行完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.end_time.not_blank}", groups = {Created.class})
    private Long endTime;

    @Schema(description = "请求总耗时", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.request_duration.not_blank}", groups = {Created.class})
    private Long requestDuration;

    @Schema(description = "报告状态/SUCCESS/ERROR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_report.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "触发方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.trigger_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(description = "执行模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.run_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    private String runMode;

    @Schema(description = "资源池", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    private String poolId;

    @Schema(description = "版本fk")
    private String versionId;

    @Schema(description = "是否是集成报告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.integrated.not_blank}", groups = {Created.class})
    private Boolean integrated;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "环境")
    private String environmentId;

    @Schema(description = "失败数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.error_count.not_blank}", groups = {Created.class})
    private Long errorCount;

    @Schema(description = "误报数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.fake_error_count.not_blank}", groups = {Created.class})
    private Long fakeErrorCount;

    @Schema(description = "未执行数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.pending_count.not_blank}", groups = {Created.class})
    private Long pendingCount;

    @Schema(description = "成功数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.success_count.not_blank}", groups = {Created.class})
    private Long successCount;

    @Schema(description = "总断言数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.assertion_count.not_blank}", groups = {Created.class})
    private Long assertionCount;

    @Schema(description = "失败断言数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.pass_assertions_count.not_blank}", groups = {Created.class})
    private Long passAssertionsCount;

    @Schema(description = "请求执行率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.request_execution_rate.not_blank}", groups = {Created.class})
    private Long requestExecutionRate;

    @Schema(description = "请求通过率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.request_approval_rate.not_blank}", groups = {Created.class})
    private Long requestApprovalRate;

    @Schema(description = "断言通过率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report.assertion_pass_rate.not_blank}", groups = {Created.class})
    private Long assertionPassRate;

    @Schema(description = "脚本标识")
    private String scriptIdentifier;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        startTime("start_time", "startTime", "BIGINT", false),
        endTime("end_time", "endTime", "BIGINT", false),
        requestDuration("request_duration", "requestDuration", "BIGINT", false),
        status("status", "status", "VARCHAR", true),
        triggerMode("trigger_mode", "triggerMode", "VARCHAR", false),
        runMode("run_mode", "runMode", "VARCHAR", false),
        poolId("pool_id", "poolId", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        integrated("integrated", "integrated", "BIT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        environmentId("environment_id", "environmentId", "VARCHAR", false),
        errorCount("error_count", "errorCount", "BIGINT", false),
        fakeErrorCount("fake_error_count", "fakeErrorCount", "BIGINT", false),
        pendingCount("pending_count", "pendingCount", "BIGINT", false),
        successCount("success_count", "successCount", "BIGINT", false),
        assertionCount("assertion_count", "assertionCount", "BIGINT", false),
        passAssertionsCount("pass_assertions_count", "passAssertionsCount", "BIGINT", false),
        requestExecutionRate("request_execution_rate", "requestExecutionRate", "BIGINT", false),
        requestApprovalRate("request_approval_rate", "requestApprovalRate", "BIGINT", false),
        assertionPassRate("assertion_pass_rate", "assertionPassRate", "BIGINT", false),
        scriptIdentifier("script_identifier", "scriptIdentifier", "VARCHAR", false);

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