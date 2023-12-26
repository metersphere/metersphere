package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class BatchEditReviewerRequest extends BaseReviewCaseBatchRequest {

    @Schema(description = "评审人id列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> reviewerId;

    @Schema(description = "是否追加", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean append;

}
