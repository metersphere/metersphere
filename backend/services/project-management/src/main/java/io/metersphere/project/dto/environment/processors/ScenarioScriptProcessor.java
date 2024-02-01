package io.metersphere.project.dto.environment.processors;


import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonTypeName("SCENARIO_SCRIPT")
public class ScenarioScriptProcessor extends ScriptProcessor {

    /**
     * 是否关联场景结果，默认否
     */
    @Schema(description = "是否关联场景结果，默认否")
    private Boolean associateScenarioResult = false;
}

