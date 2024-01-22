package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MinderReviewFunctionalCasePageRequest extends ReviewFunctionalCasePageRequest{

    @Schema(description = "我的评审", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean viewResult;
}
