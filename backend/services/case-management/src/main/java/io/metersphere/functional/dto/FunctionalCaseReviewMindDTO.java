package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class FunctionalCaseReviewMindDTO extends FunctionalCaseMindDTO{

    @Schema(description =  "用例评审ID--用例评审脑图")
    private String reviewId;

    @Schema(description =  "功能用例ID--用例评审脑图")
    private String caseId;
}
