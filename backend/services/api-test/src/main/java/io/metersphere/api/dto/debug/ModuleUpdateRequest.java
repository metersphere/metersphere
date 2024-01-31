package io.metersphere.api.dto.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModuleUpdateRequest {
    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug_module.id.length_range}")
    private String id;

    @Schema(description = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{file_module.name.not_blank}")
    @Pattern(regexp = "^[^\\\\/]*$", message = "{api_debug_module.name.not_contain_slash}")
    @Size(min = 1, max = 255, message = "{api_debug_module.name.length_range}")
    private String name;
}

