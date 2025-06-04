package io.metersphere.functional.dto;

import lombok.Data;

@Data
public class FunctionalCaseAIPromptDTO {

    /**
     * AI模型用例生成方法提示词
     */
    private FunctionalCaseAIDesignPromptDTO designPrompt;

    /**
     * AI模型生成用例的提示语
     */
    private FunctionalCaseAITemplatePromptDTO templatePrompt;
}
