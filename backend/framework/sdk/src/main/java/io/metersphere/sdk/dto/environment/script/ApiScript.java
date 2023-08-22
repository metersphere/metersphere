package io.metersphere.sdk.dto.environment.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
public class ApiScript {
    @Schema(description = "环境级")
    private ScriptContent envJSR223PostScript;
    @Schema(description = "场景级")
    private ScenarioPostScript scenarioJSR223PostScript;
    @Schema(description = "步骤级")
    private StepPostScript stepJSR223PostScript;


}
@Data
@EqualsAndHashCode(callSuper = true)
class ScenarioPostScript extends ScriptContent {

    @Schema(description = "关联场景结果 true: 是/false: 否")
    private Boolean associateScenarioResults = false;
}

@Data
@EqualsAndHashCode(callSuper = true)
class StepPostScript extends ScriptContent{

    @Schema(description = "忽略请求")
    private List<String> filterRequestPostScript;
    @Schema(description = "脚本执行顺序 true:先执行 false:后执行")
    private Boolean preScriptExecBefore = true;
    @Schema(description = "脚本内容")
    private ScriptContent scriptContent;

}
