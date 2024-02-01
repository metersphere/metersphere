package io.metersphere.project.dto.environment.processors;

import io.metersphere.project.api.processor.MsProcessor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 接口测试-场景级前后置配置
 * @Author: jianxing
 * @CreateTime: 2024-02-01  14:53
 */
@Data
public class ApiEnvPlanProcessorConfig {

    // 预留其他配置

    @Schema(description = "前后置列表")
    private List<MsProcessor> processors;
}
