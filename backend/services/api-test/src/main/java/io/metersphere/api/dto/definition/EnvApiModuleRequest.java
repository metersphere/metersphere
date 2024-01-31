package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EnvApiModuleRequest implements Serializable {
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition_module.project_id.length_range}")
    private String projectId;

    @Schema(description = "选中的模块")
    private List<ApiModuleDTO> selectedModules;

    private static final long serialVersionUID = 1L;

}
