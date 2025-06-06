package io.metersphere.api.dto.definition;


import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiTestCaseAiDTO {

    @Schema(description = "请求参数")
    private MsHTTPElement msHTTPElement;

    @Schema(description = "前后置配置")
    private MsCommonElement processorConfig;
}
