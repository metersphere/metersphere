package io.metersphere.functional.request;


import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class RelationshipAddRequest extends BaseFunctionalCaseBatchDTO {

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String id;

    @Schema(description = "类型前置/后置", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"PRE", "POST"})
    @NotBlank(message = "{functional_case_relationship_edge.type.not_blank}")
    private String type;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;
}
