package io.metersphere.project.dto.environment.processors;


import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonTypeName("ENV_REQUEST_SCRIPT")
public class EnvRequestScriptProcessor extends ScriptProcessor {

    /**
     * 忽略的请求协议列表
     */
    @Schema(description = "忽略请求")
    private List<String> ignoreProtocols = new ArrayList<>(0);
    /**
     * 是否是步骤内前置脚本前
     */
    @Schema(description = "是否是步骤内前置脚本前")
    private Boolean beforeStepScript = true;
}

