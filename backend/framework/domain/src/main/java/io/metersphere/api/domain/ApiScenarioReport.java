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
    @Size(min = 1, max = 300, message = "{api_scenario_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "测试计划关联场景表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.test_plan_scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.test_plan_scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanScenarioId;

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

    @Schema(description = "开始时间/同创建时间一致", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.start_time.not_blank}", groups = {Created.class})
    private Long startTime;

    @Schema(description = "结束时间/报告执行完成")
    private Long endTime;

    @Schema(description = "请求总耗时", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.request_duration.not_blank}", groups = {Created.class})
    private Long requestDuration;

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

    @Schema(description = "是否是集成报告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.integrated.not_blank}", groups = {Created.class})
    private Boolean integrated;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "环境")
    private String environmentId;

    @Schema(description = "失败数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.error_count.not_blank}", groups = {Created.class})
    private Long errorCount;

    @Schema(description = "误报数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.fake_error_count.not_blank}", groups = {Created.class})
    private Long fakeErrorCount;

    @Schema(description = "未执行数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.pending_count.not_blank}", groups = {Created.class})
    private Long pendingCount;

    @Schema(description = "成功数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.success_count.not_blank}", groups = {Created.class})
    private Long successCount;

    @Schema(description = "总断言数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.assertion_count.not_blank}", groups = {Created.class})
    private Long assertionCount;

    @Schema(description = "成功断言数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.assertion_success_count.not_blank}", groups = {Created.class})
    private Long assertionSuccessCount;

    @Schema(description = "请求失败率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.request_error_rate.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.request_error_rate.length_range}", groups = {Created.class, Updated.class})
    private String requestErrorRate;

    @Schema(description = "请求未执行率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.request_pending_rate.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.request_pending_rate.length_range}", groups = {Created.class, Updated.class})
    private String requestPendingRate;

    @Schema(description = "请求误报率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.request_fake_error_rate.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.request_fake_error_rate.length_range}", groups = {Created.class, Updated.class})
    private String requestFakeErrorRate;

    @Schema(description = "请求通过率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.request_pass_rate.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.request_pass_rate.length_range}", groups = {Created.class, Updated.class})
    private String requestPassRate;

    @Schema(description = "断言通过率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.assertion_pass_rate.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.assertion_pass_rate.length_range}", groups = {Created.class, Updated.class})
    private String assertionPassRate;

    @Schema(description = "脚本标识")
    private String scriptIdentifier;

    @Schema(description = "等待时间")
    private Long waitingTime;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report.exec_status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_scenario_report.exec_status.length_range}", groups = {Created.class, Updated.class})
    private String execStatus;

    @Schema(description = "是否是测试计划整体执行", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_report.plan.not_blank}", groups = {Created.class})
    private Boolean plan;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        testPlanScenarioId("test_plan_scenario_id", "testPlanScenarioId", "VARCHAR", false),
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
        integrated("integrated", "integrated", "BIT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        environmentId("environment_id", "environmentId", "VARCHAR", false),
        errorCount("error_count", "errorCount", "BIGINT", false),
        fakeErrorCount("fake_error_count", "fakeErrorCount", "BIGINT", false),
        pendingCount("pending_count", "pendingCount", "BIGINT", false),
        successCount("success_count", "successCount", "BIGINT", false),
        assertionCount("assertion_count", "assertionCount", "BIGINT", false),
        assertionSuccessCount("assertion_success_count", "assertionSuccessCount", "BIGINT", false),
        requestErrorRate("request_error_rate", "requestErrorRate", "VARCHAR", false),
        requestPendingRate("request_pending_rate", "requestPendingRate", "VARCHAR", false),
        requestFakeErrorRate("request_fake_error_rate", "requestFakeErrorRate", "VARCHAR", false),
        requestPassRate("request_pass_rate", "requestPassRate", "VARCHAR", false),
        assertionPassRate("assertion_pass_rate", "assertionPassRate", "VARCHAR", false),
        scriptIdentifier("script_identifier", "scriptIdentifier", "VARCHAR", false),
        waitingTime("waiting_time", "waitingTime", "BIGINT", false),
        execStatus("exec_status", "execStatus", "VARCHAR", false),
        plan("plan", "plan", "BIT", true);

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