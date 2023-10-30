package io.metersphere.api.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DebugModuleCreateRequest {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(description = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{file_module.name.not_blank}")
    @Pattern(regexp = "^[^\\\\/]*$", message = "{api_debug_module.name.not_contain_slash}")
    private String name;

    @Schema(description = "父模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{parent.node.not_blank}")
    private String parentId = ModuleConstants.ROOT_NODE_PARENT_ID;

    @Schema(description = "协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug_module.protocol.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_debug.protocol.length_range}", groups = {Created.class, Updated.class})
    private String protocol = ModuleConstants.NODE_PROTOCOL_HTTP;
}

