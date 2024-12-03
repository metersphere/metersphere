package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiScenarioMessageDTO {
    @Schema(description = "message.domain.id")
    private String id;

    @Schema(description = "message.domain.api_scenario_name")
    private String name;

    @Schema(description = "message.domain.api_scenario_priority")
    private String priority;

    @Schema(description = "message.domain.api_scenario_status")
    private String status;

    @Schema(description = "message.domain.api_scenario_stepTotal")
    private Integer stepTotal;

    @Schema(description = "message.domain.api_scenario_requestPassRate")
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

    @Schema(description = "message.domain.api_scenario_reportUrl")
    private String reportUrl;

    @Schema(description = "message.domain.api_scenario_shareUrl")
    private String shareUrl;

    @Schema(description = "message.domain.api_scenario_reportName")
    private String reportName;

    @Schema(description = "message.domain.api_scenario_startTime")
    private Long startTime;

    @Schema(description = "message.domain.api_scenario_endTime")
    private Long endTime;

    @Schema(description = "message.domain.api_scenario_requestDuration")
    private Long requestDuration; //请求总耗时

    @Schema(description = "message.domain.api_scenario_reportStatus")
    private String reportStatus;

    @Schema(description = "message.domain.api_scenario_environment")
    private String environment;

    @Schema(description = "message.domain.api_scenario_errorCount")
    private Long errorCount;

    @Schema(description = "message.domain.api_scenario_fakeErrorCount")
    private Long fakeErrorCount;

    @Schema(description = "message.domain.api_scenario_pendingCount")
    private Long pendingCount;

    @Schema(description = "message.domain.api_scenario_successCount")
    private Long successCount;

    @Schema(description = "message.domain.api_scenario_assertionCount")
    private Long assertionCount;

    @Schema(description = "message.domain.api_scenario_assertionSuccessCount")
    private Long assertionSuccessCount;

    @Schema(description = "message.domain.api_scenario_requestErrorRate")
    private String requestErrorRate;

    @Schema(description = "message.domain.api_scenario_requestPendingRate")
    private String requestPendingRate;

    @Schema(description = "message.domain.api_scenario_requestFakeErrorRate")
    private String requestFakeErrorRate;

    @Schema(description = "message.domain.api_scenario_assertionPassRate")
    private String assertionPassRate;


}
