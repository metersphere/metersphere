package io.metersphere.functional.dto;

import io.metersphere.functional.domain.CaseReview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class CaseReviewDTO extends CaseReview {

    @Schema(description = "评审人")
    private List<CaseReviewUserDTO> reviewers;

    @Schema(description = "通过数")
    private int passCount;

    @Schema(description = "不通过数")
    private int unPassCount;

    @Schema(description = "重新提审数")
    private int reReviewedCount;

    @Schema(description = "评审中数")
    private int underReviewedCount;

    @Schema(description = "已评审过得用例数")
    private int reviewedCount;

    @Schema(description = "关注标识")
    private Boolean followFlag;

    @Schema(description = "所属模块名称")
    private String moduleName;

}
