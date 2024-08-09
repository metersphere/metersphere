package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiDefinitionMockConfig;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApiMockWithBlob extends ApiDefinitionMockConfig {

    @Schema(description = "mock名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_definition_mock.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "自定义标签")
    private java.util.List<String> tags;

    @Schema(description = "启用/禁用")
    private Boolean enable;

    @Schema(description = "")
    private Integer statusCode;

    @Schema(description = "接口定义ID")
    private String apiDefinitionId;
}
