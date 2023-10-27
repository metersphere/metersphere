package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FunctionalCaseMessageDTO {

    @Schema(description ="message.domain.name")
    private String name;

    @Schema(description = "message.domain.test_plan_name")
    private String testPlanName;

    @Schema(description = "message.domain.review_name")
    private String reviewName;

    @Schema(description = "message.domain.review_status")
    private String reviewStatus;

    @Schema(description = "message.domain.case_model")
    private String caseModel;

    @Schema(description = "message.domain.last_execute_result")
    private String lastExecuteResult;

    @Schema(description = "message.domain.create_user")
    private String createUser;

    @Schema(description = "message.domain.update_user")
    private String updateUser;

    @Schema(description = "message.domain.delete_user")
    private String deleteUser;

    @Schema(description = "message.domain.create_time")
    private Long createTime;

    @Schema(description = "message.domain.update_time")
    private Long updateTime;

    @Schema(description = "message.domain.delete_time")
    private Long deleteTime;

}
