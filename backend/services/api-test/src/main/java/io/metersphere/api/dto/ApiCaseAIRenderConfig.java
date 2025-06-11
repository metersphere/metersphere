package io.metersphere.api.dto;

import io.metersphere.system.dto.AIRenderConfig;
import lombok.Data;

@Data
public class ApiCaseAIRenderConfig extends AIRenderConfig {
    private Boolean headers = true;
    private Boolean query = true;
    private Boolean rest = true;

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

    private String apiDefinition;
    private String apiResponses;
    private String userMessage;
}
