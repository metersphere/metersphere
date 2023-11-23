package io.metersphere.api.dto.definition;

import io.metersphere.sdk.constants.ModuleConstants;
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
public class ApiDefinitionDeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}")
    private String id;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}")
    private String projectId;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.protocol.not_blank}")
    @Size(min = 1, max = 20, message = "{api_debug.protocol.length_range}")
    private String protocol = ModuleConstants.NODE_PROTOCOL_HTTP;

    @Schema(description = "删除列表版本/删除全部版本")
    private Boolean deleteAll = false;


}
