package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class FunctionalCaseDeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String id;

    @Schema(description = "删除列表版本/删除全部版本")
    private Boolean deleteAll = true;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

}
