package io.metersphere.api.dto;

import io.metersphere.api.dto.definition.ApiAiCaseDTO;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.system.dto.AIRenderConfig;
import lombok.Data;

@Data
public class ApiCaseAIRenderConfig extends AIRenderConfig {
    private Boolean body = true;
    private Boolean wwwFormBody = false;
    private Boolean fromDataBody = false;
    private Boolean jsonBody = false;
    private Boolean xmlBody = false;
    private Boolean rawBody = false;

    private Boolean asserts = true;
    private Boolean jsonPathAssert = false;
    private Boolean xpathAssert = false;

    private Boolean preScript = true;
    private Boolean postScript = true;

    private String userMessage;

    private ApiAiCaseDTO api;
    private HttpResponse response;
    private String textBodyValue;
    private String textResponseBodyValue;
    private String apiName;
}
