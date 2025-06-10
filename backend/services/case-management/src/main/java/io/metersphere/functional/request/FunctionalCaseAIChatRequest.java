package io.metersphere.functional.request;

import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseAIChatRequest extends AIChatRequest {
    //项目id
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    //模块id
    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleId;

    //模板id
    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateId;
}
