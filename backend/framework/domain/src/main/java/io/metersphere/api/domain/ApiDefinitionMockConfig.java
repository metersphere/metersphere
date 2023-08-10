package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinitionMockConfig implements Serializable {
    @Schema(description =  "接口mock pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock_config.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock_config.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "请求内容")
    private byte[] request;

    @Schema(description =  "响应内容")
    private byte[] response;

    private static final long serialVersionUID = 1L;
}