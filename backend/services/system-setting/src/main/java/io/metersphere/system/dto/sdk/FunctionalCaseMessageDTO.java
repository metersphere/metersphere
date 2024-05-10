package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FunctionalCaseMessageDTO {

    @Schema(description ="message.domain.name")
    private String name;

    @Schema(description = "message.domain.testPlanName")
    private String testPlanName;

    @Schema(description = "message.domain.reviewName")
    private String reviewName;

    @Schema(description = "message.domain.reviewStatus")
    private String reviewStatus;

    @Schema(description = "message.domain.caseModel")
    private String caseEditType;

    @Schema(description = "message.domain.lastExecuteResult")
    private String lastExecuteResult;

    @Schema(description = "message.domain.createUser")
    private String createUser;

    @Schema(description = "message.domain.updateUser")
    private String updateUser;

    @Schema(description = "message.domain.deleteUser")
    private String deleteUser;

    @Schema(description = "message.domain.createTime")
    private Long createTime;

    @Schema(description = "message.domain.updateTime")
    private Long updateTime;

    @Schema(description = "message.domain.deleteTime")
    private Long deleteTime;

    @Schema(description = "message.domain.deleteTime")
    private String triggerMode;

}
