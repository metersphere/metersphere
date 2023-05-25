package io.metersphere.api.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "mock期望值配置")
@Table("api_definition_mock_config")
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionMockConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_definition_mock_config.api_definition_mock_id.not_blank}", groups = {Updated.class})
    @Schema(title = "接口mock pk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(title = "请求内容", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] request;

    @Schema(title = "响应内容", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] response;

}