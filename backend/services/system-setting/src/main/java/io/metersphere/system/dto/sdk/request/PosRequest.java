package io.metersphere.system.dto.sdk.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PosRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "移动用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.source_id.not_blank}")
    private String moveId;

    @Schema(description = "目标用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.target_id.not_blank}")
    private String targetId;

    @Schema(description = "移动类型", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"BEFORE", "AFTER", "APPEND"})
    @NotBlank(message = "{case_review.moveMode.not_blank}")
    private String moveMode;
}
