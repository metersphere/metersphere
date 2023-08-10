package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionDTO extends ApiDefinition {
    @Schema(description =  "请求内容")
    private byte[] request;

    @Schema(description =  "响应内容")
    private byte[] response;

    @Schema(description =  "备注")
    private byte[] remark;
}
