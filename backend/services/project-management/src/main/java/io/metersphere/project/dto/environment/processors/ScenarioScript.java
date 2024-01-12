package io.metersphere.project.dto.environment.processors;


import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonTypeName("SCENARIO_SCRIPT")
public class ScenarioScript extends ScriptProcessor {

    @Schema(description = "关联场景结果 true: 是/false: 否")
    private Boolean associateScenarioResults = false;
}

