package io.metersphere.system.dto.request.ai;

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

    public AIChatOption withPrompt(@NotBlank String prompt) {
        this.prompt = prompt;
        return this;
    }
}
