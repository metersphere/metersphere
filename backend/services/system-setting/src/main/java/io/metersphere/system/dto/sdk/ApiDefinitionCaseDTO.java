package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiDefinitionCaseDTO {
    @Schema(description = "message.domain.id")
    private String id;

    @Schema(description = "message.domain.name")
    private String name;

    @Schema(description = "message.domain.protocol")
    private String protocol;

    @Schema(description = "message.domain.method")
    private String method;

    @Schema(description = "message.domain.path")
    private String path;

    @Schema(description = "message.domain.status")
    private String status;

    @Schema(description = "message.domain.description")
    private String description;

    @Schema(description = "message.domain.createTime")
    private Long createTime;

    @Schema(description = "message.domain.createUser")
    private String createUser;

    @Schema(description = "message.domain.updateTime")
    private Long updateTime;

    @Schema(description = "message.domain.updateUser")
    private String updateUser;

    @Schema(description = "message.domain.deleteUser")
    private String deleteUser;

    @Schema(description = "message.domain.deleteTime")
    private Long deleteTime;

    @Schema(description = "message.domain.caseName")
    private String caseName;

    @Schema(description = "message.domain.priority")
    private String priority;

    @Schema(description = "message.domain.caseStatus")
    private String caseStatus;

    @Schema(description = "message.domain.lastReportStatus")
    private String lastReportStatus;

    @Schema(description = "message.domain.principal")
    private String principal;

    @Schema(description = "message.domain.caseCreateTime")
    private Long caseCreateTime;

    @Schema(description = "message.domain.caseCreateUser")
    private String caseCreateUser;

    @Schema(description = "message.domain.caseUpdateTime")
    private Long caseUpdateTime;

    @Schema(description = "message.domain.caseUpdateUser")
    private String caseUpdateUser;

    @Schema(description = "message.domain.caseDeleteTime")
    private Long caseDeleteTime;

    @Schema(description = "message.domain.caseDeleteUser")
    private String caseDeleteUser;

    @Schema(description = "message.domain.projectId")
    private String projectId;

    @Schema(description = "message.domain.mockName")
    private String mockName;

    @Schema(description = "message.domain.reportUrl")
    private String reportUrl;

    @Schema(description = "message.domain.shareUrl")
    private String shareUrl;

    @Schema(description = "message.domain.reportName")
    private String reportName;

    @Schema(description = "message.domain.startTime")
    private Long startTime;

    @Schema(description = "message.domain.endTime")
    private Long endTime;

    @Schema(description = "message.domain.requestDuration")
    private Long requestDuration;

    @Schema(description = "message.domain.reportStatus")
    private String reportStatus;

    @Schema(description = "message.domain.environment")
    private String environment;

    @Schema(description = "message.domain.errorCount")
    private Long errorCount;

    @Schema(description = "message.domain.fakeErrorCount")
    private Long fakeErrorCount;

    @Schema(description = "message.domain.pendingCount")
    private Long pendingCount;

    @Schema(description = "message.domain.successCount")
    private Long successCount;

    @Schema(description = "message.domain.assertionCount")
    private Long assertionCount;

    @Schema(description = "message.domain.assertionSuccessCount")
    private Long assertionSuccessCount;

    @Schema(description = "message.domain.requestErrorRate")
    private String requestErrorRate;

    @Schema(description = "message.domain.requestPendingRate")
    private String requestPendingRate;

    @Schema(description = "message.domain.requestFakeErrorRate")
    private String requestFakeErrorRate;

    @Schema(description = "message.domain.requestPassRate")
    private String requestPassRate;

    @Schema(description = "message.domain.assertionPassRate")
    private String assertionPassRate;

}
