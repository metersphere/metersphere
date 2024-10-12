package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiTestCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiTestCaseAssociateDTO extends ApiTestCase {

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "协议")
    private String protocol;
}
