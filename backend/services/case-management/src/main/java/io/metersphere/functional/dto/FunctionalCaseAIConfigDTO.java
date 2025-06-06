package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseAIConfigDTO {

    /**
     * AI模型用例生成方法提示词配置
     */
    @Schema(description = "AI模型用例生成方法提示词配置")
    private FunctionalCaseAIDesignConfigDTO designConfig;

    /**
     * AI模型生成用例的提示词配置
     */
    @Schema(description = "AI模型生成用例的提示词配置")
    private FunctionalCaseAITemplateConfigDTO templateConfig;
}
