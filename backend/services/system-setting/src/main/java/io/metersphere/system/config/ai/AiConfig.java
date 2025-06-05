package io.metersphere.system.config.ai;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: jianxing
 * @CreateTime: 2025-05-27  18:36
 */
@Configuration
public class AiConfig {

    @Bean
    MessageChatMemoryAdvisor messageChatMemoryAdvisor(MsMessageChatMemory msMessageChatMemory) {
        return MessageChatMemoryAdvisor.builder(msMessageChatMemory).build();
    }
}
