package io.metersphere.project.dto.environment.processors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 环境的前后置配置
 */
@Data
public class EnvProcessorConfig {
    @Schema(description = "接口测试前后置配置")
    private ApiEnvProcessorConfig apiProcessorConfig;
}
