package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PosRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "目标用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.name.not_blank}")
    private String targetId;

    @Schema(description = "移动类型", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"BEFORE", "AFTER", "APPEND"})
    @NotBlank(message = "{case_review.module_id.not_blank}")
    private String moveMode;

    @Schema(description = "移动用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.module_id.not_blank}")
    private String moveId;
}
