package io.metersphere.api.dto.definition;

import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiTestCaseAiAddRequest extends AIChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug.project_id.length_range}")
    private String projectId;

    @Schema(description = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.api_definition_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_test_case.api_definition_id.length_range}")
    private String apiDefinitionId;

}
