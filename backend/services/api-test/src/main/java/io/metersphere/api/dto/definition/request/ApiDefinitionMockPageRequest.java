package io.metersphere.api.dto.definition.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionMockPageRequest extends BasePageRequest {

    @Schema(description = "接口 mock 名称")
    @Size(min = 1, max = 255, message = "{api_definition_mock.name.length_range}")
    private String name;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition_mock.project_id.length_range}")
    private String projectId;

    @Schema(description = "启用/禁用(状态为 false 时为禁用)")
    private boolean enable = true;

    @Schema(description = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 50, message = "{api_definition_mock.api_definition_id.length_range}")
    private String apiDefinitionId;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols = new ArrayList<>();

    @Schema(description = "模块ID")
    private List<@NotBlank String> moduleIds;

}
