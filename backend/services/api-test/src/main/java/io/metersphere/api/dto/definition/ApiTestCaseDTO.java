package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiTestCaseDTO extends ApiTestCase {
    @Schema(description = "是否关注")
    private Boolean follow;
    @Schema(description = "请求内容")
    private AbstractMsTestElement request;
}
