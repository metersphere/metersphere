package io.metersphere.api.dto.definition.importdto;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionImportDTO extends ApiDefinition {

    @Schema(description = "请求内容")
    private AbstractMsTestElement request;

    @Schema(description = "响应内容")
    private List<HttpResponse> response;

    @Schema(description = "模块path")
    private String modulePath;

}
