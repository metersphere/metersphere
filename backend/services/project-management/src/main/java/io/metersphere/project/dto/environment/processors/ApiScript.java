package io.metersphere.project.dto.environment.processors;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiScript {
    @Schema(description = "测试计划级 包括脚本和sql")
    private List<MsProcessor> planProcessors;
    @Schema(description = "场景级  包括脚本和sql")
    private List<MsProcessor> scenarioProcessors;
    @Schema(description = "步骤级  包括脚本和sql 脚本是脚本前和脚本后")
    private List<MsProcessor> stepProcessors;
}
