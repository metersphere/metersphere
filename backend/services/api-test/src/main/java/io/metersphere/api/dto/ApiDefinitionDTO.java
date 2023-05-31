package io.metersphere.api.dto;

import io.metersphere.api.domain.ApiDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionDTO extends ApiDefinition {
    @Schema(title = "请求内容")
    private byte[] request;

    @Schema(title = "响应内容")
    private byte[] response;

    @Schema(title = "备注")
    private byte[] remark;
}
