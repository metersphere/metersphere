package io.metersphere.api.dto.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionPageRequest extends BasePageRequest {

    private static final long serialVersionUID = 1L;

    @Schema(description = "接口pk")
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}")
    private String id;

    @Schema(description =  "接口名称")
    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}")
    private String name;

    @Schema(description =  "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.protocol.not_blank}")
    @Size(min = 1, max = 20, message = "{api_definition.protocol.length_range}")
    private String protocol = ModuleConstants.NODE_PROTOCOL_HTTP;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}")
    private String projectId;

    @Schema(description = "版本fk")
    @Size(min = 1, max = 50, message = "{api_definition.version_id.length_range}")
    private String versionId;

    @Schema(description = "版本引用fk")
    @Size(min = 1, max = 50, message = "{api_definition.ref_id.length_range}")
    private String refId;
}
