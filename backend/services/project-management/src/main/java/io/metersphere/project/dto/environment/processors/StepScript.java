package io.metersphere.project.dto.environment.processors;


import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("STEP_SCRIPT")
public class StepScript extends ScriptProcessor {
    @Schema(description = "忽略请求")
    private List<String> filterRequestScript;
    @Schema(description = "脚本执行顺序 true:先执行 false:后执行")
    private Boolean scriptExecBefore = true;
}
