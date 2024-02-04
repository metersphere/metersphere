package io.metersphere.project.dto.environment.processors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 环境的前后置配置
 */
@Data
public class EnvProcessorConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口测试前后置配置")
    private ApiEnvProcessorConfig apiProcessorConfig = new ApiEnvProcessorConfig();
}
