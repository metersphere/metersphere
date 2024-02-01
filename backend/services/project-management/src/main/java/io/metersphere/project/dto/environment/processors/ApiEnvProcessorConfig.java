package io.metersphere.project.dto.environment.processors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-01  14:49
 */
@Data
public class ApiEnvProcessorConfig {
    @Schema(description = "测试计划级前后置配置")
    private ApiEnvPlanProcessorConfig planProcessorConfig;
    @Schema(description = "场景级前后置配置")
    private ApiEnvScenarioProcessorConfig scenarioProcessorConfig;
    @Schema(description = "请求级前后置配置")
    private ApiEnvRequestProcessorConfig requestProcessorConfig;
}
