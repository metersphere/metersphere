package io.metersphere.system.ai.engine;

import io.metersphere.system.dto.request.ai.ModelConfigDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

public interface AIChatClient {
    ChatModel chatModel(ModelConfigDTO modelConfigDTO);
    ChatClient chatClient(ModelConfigDTO modelConfigDTO);
}
