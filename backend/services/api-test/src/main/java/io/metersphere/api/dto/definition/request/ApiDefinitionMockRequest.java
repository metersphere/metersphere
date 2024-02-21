package io.metersphere.api.dto.definition.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author lan
 */
@Data
public class ApiDefinitionMockRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口 mock pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition_mock.id.length_range}")
    private String id;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition_mock.project_id.length_range}")
    private String projectId;
}
