package io.metersphere.project.dto.environment.processors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-01  14:49
 */
@Data
public class ApiEnvProcessorConfig  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "测试计划级前后置配置")
    private ApiEnvPlanProcessorConfig planProcessorConfig = new ApiEnvPlanProcessorConfig();
    @Schema(description = "场景级前后置配置")
    private ApiEnvScenarioProcessorConfig scenarioProcessorConfig = new ApiEnvScenarioProcessorConfig();
    @Schema(description = "请求级前后置配置")
    private ApiEnvRequestProcessorConfig requestProcessorConfig = new ApiEnvRequestProcessorConfig();
}
