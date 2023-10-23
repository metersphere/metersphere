package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiDefinitionCaseDTO {

    @Schema(description ="message.domain.name")
    private String name;

    @Schema(description ="message.domain.protocol")
    private String protocol;

    @Schema(description ="message.domain.method")
    private String method;

    @Schema(description ="message.domain.path")
    private String path;

    @Schema(description ="message.domain.status")
    private String status;

    @Schema(description ="message.domain.description")
    private String description;

    @Schema(description ="message.domain.create_time")
    private Long createTime;

    @Schema(description ="message.domain.create_user")
    private String createUser;

    @Schema(description ="message.domain.update_time")
    private Long updateTime;

    @Schema(description ="message.domain.update_user")
    private String updateUser;

    @Schema(description ="message.domain.delete_user")
    private String deleteUser;

    @Schema(description ="message.domain.delete_time")
    private Long deleteTime;

    @Schema(description ="message.domain.case_name")
    private String caseName;

    @Schema(description ="message.domain.priority")
    private String priority;

    @Schema(description ="message.domain.case_status")
    private String caseStatus;

    @Schema(description ="message.domain.last_report_status")
    private String lastReportStatus;

    @Schema(description ="message.domain.principal")
    private String principal;

    @Schema(description ="message.domain.case_create_time")
    private Long caseCreateTime;

    @Schema(description ="message.domain.case_create_user")
    private String caseCreateUser;

    @Schema(description ="message.domain.case_update_time")
    private Long caseUpdateTime;

    @Schema(description ="message.domain.case_update_user")
    private String caseUpdateUser;

    @Schema(description ="message.domain.case_delete_time")
    private Long caseDeleteTime;

    @Schema(description ="message.domain.case_delete_user")
    private String caseDeleteUser;



}
