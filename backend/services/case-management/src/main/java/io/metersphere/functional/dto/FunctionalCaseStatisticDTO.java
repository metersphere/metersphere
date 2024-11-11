package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionalCaseStatisticDTO {

    @Schema(description = "caseID")
    private String caseId;

    @Schema(description = "fieldId")
    private String fieldId;

    @Schema(description = "评审结果:未评审(UN_REVIEWED)/评审中(UNDER_REVIEWED)/PASS(通过)/UN_PASS(未通过)/RE_REVIEWED(重新提审)")
    private String reviewStatus;

    @Schema(description =  "用例等级")
    private String priority;
}
