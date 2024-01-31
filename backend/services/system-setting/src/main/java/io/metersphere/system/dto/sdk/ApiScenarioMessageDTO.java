package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiScenarioMessageDTO {

    @Schema(description = "message.domain.api_scenario_name")
    private String name;

    @Schema(description = "message.domain.api_scenario_priority")
    private String priority;

    @Schema(description = "message.domain.api_scenario_status")
    private String status;

    @Schema(description = "message.domain.api_scenario_stepTotal")
    private Integer stepTotal;

    @Schema(description = "message.domain.api_scenario_passRate")
    private String requestPassRate;

    @Schema(description = "message.domain.api_scenario_lastReportStatus")
    private String lastReportStatus;

    @Schema(description = "message.domain.api_scenario_description")
    private String description;

    @Schema(description = "message.domain.api_scenario_tags")
    private java.util.List<String> tags;

    @Schema(description = "message.domain.api_scenario_grouped")
    private Boolean grouped;

    @Schema(description = "message.domain.api_scenario_createUser")
    private String createUser;

    @Schema(description = "message.domain.api_scenario_createTime")
    private Long createTime;

    @Schema(description = "message.domain.api_scenario_deleteTime")
    private Long deleteTime;

    @Schema(description = "message.domain.api_scenario_deleteUser")
    private String deleteUser;

    @Schema(description = "message.domain.api_scenario_updateUser")
    private String updateUser;

    @Schema(description = "message.domain.api_scenario_updateTime")
    private Long updateTime;

    @Schema(description = "message.domain.scenario_report_url")
    private String reportUrl;

    @Schema(description = "message.domain.scenario_report_share_url")
    private String shareUrl;

    @Schema(description = "message.domain.scenario_report_name")
    private String reportName;

    @Schema(description = "message.domain.scenario_report_start_time")
    private Long startTime;

    @Schema(description = "message.domain.scenario_report_end_time")
    private Long endTime;

    @Schema(description = "message.domain.scenario_report_request_duration")
    private Long requestDuration;

    @Schema(description = "message.domain.scenario_report_status")
    private String reportStatus;

    @Schema(description = "message.domain.scenario_report_environment")
    private String environment;

    @Schema(description = "message.domain.scenario_report_error_count")
    private Long errorCount;

    @Schema(description = "message.domain.scenario_report_fake_error_count")
    private Long fakeErrorCount;

    @Schema(description = "message.domain.scenario_report_pending_count")
    private Long pendingCount;

    @Schema(description = "message.domain.scenario_report_success_count")
    private Long successCount;

    @Schema(description = "message.domain.scenario_report_assertion_count")
    private Long assertionCount;

    @Schema(description = "message.domain.scenario_report_assertion_success_count")
    private Long assertionSuccessCount;

    @Schema(description = "message.domain.scenario_report_request_error_rate")
    private String requestErrorRate;

    @Schema(description = "message.domain.scenario_report_request_pending_rate")
    private String requestPendingRate;

    @Schema(description = "message.domain.scenario_report_request_fake_error_rate")
    private String requestFakeErrorRate;

    @Schema(description = "message.domain.scenario_report_request_pass_rate")
    private String reportRequestPassRate;

    @Schema(description = "message.domain.scenario_report_assertion_pass_rate")
    private String assertionPassRate;


}
