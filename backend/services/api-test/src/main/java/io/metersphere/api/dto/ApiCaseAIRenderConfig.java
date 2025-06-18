package io.metersphere.api.dto;

import io.metersphere.api.dto.definition.ApiAiCaseDTO;
import io.metersphere.system.dto.AIRenderConfig;
import lombok.Data;

@Data
public class ApiCaseAIRenderConfig extends AIRenderConfig {
    private Boolean body = true;
    private Boolean wwwFormBody = false;
    private Boolean formDataBody = false;
    private Boolean jsonBody = false;
    private Boolean xmlBody = false;
    private Boolean rawBody = false;

    private Boolean assertion = true;

    private Boolean preScript = true;
    private Boolean postScript = true;

    private String userMessage;

    private ApiAiCaseDTO api;
    private String textBodyValue;
    private String apiName;

    private Boolean normal;
    private Boolean abnormal;
}
