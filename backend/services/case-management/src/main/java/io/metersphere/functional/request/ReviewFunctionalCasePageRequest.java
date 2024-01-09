package io.metersphere.functional.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class ReviewFunctionalCasePageRequest extends BasePageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_user.review_id.not_blank}")
    private String reviewId;

    @Schema(description = "用例所在项目ID(默认当前项目)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_user.project_id.not_blank}")
    private String projectId;

    @Schema(description = "是否只看我的", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean viewFlag;

    @Schema(description = "模块id")
    private List<String> moduleIds;
}
