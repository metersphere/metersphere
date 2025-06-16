package io.metersphere.system.dto.request.ai;

import io.metersphere.ai.engine.utils.TextCleaner;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
public class AIChatOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String conversationId;

    private AiModelSourceDTO module;

    private String prompt;

    private String system;

    public String getPrompt() {
        // 过滤，简化提示词
        return TextCleaner.fullClean(this.prompt);
    }

    public AIChatOption withPrompt(@NotBlank String prompt) {
        this.prompt = prompt;
        return this;
    }
}
