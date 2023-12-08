package io.metersphere.functional.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class BaseReviewCaseBatchRequest extends TableBatchProcessDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewId;


    @Schema(description = "userId用来判断是否只看我的")
    private String userId;
}
