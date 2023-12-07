package io.metersphere.functional.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class ReviewFunctionalCasePageRequest extends BasePageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewId;


    @Schema(description = "是否只看我的", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean viewFlag;
}
