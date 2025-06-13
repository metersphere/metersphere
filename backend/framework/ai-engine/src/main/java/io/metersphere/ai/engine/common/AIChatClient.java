package io.metersphere.ai.engine.common;

import io.metersphere.ai.engine.advisor.LoggingCallAdvisor;
import io.metersphere.ai.engine.advisor.LoggingStreamAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;

import java.util.Optional;

/**
 * AI Chat Model
 */
public abstract class AIChatClient {

    public abstract ChatClient chatClient(AIChatOptions options);

    public abstract ChatModel chatModel(AIChatOptions options);

    /**
     * 添加顾问
     *
     * @param options AIChatOptions 对象
     * @param builder ChatClient.Builder 对象
     */
    public void addAdvisor(AIChatOptions options, ChatClient.Builder builder) {
        Optional.of(options)
                .filter(opt -> !opt.isDisableLoggingAdvisor())
                .ifPresent(opt ->
                        builder.defaultAdvisors(new LoggingStreamAdvisor(), new LoggingCallAdvisor()));
    }

    /**
     * 构建 OpenAiChatOptions.Builder 实例
     *
     * @param options AIChatOptions 对象
     * @return OpenAiChatOptions.Builder 实例
     */
    public ChatOptions.Builder builderChatOptions(AIChatOptions options) {
        // 使用单一的 OpenAiChatOptions.Builder 配置所有参数
        ChatOptions.Builder optionsBuilder = ChatOptions.builder();

        // 温度设置
        Optional.of(options.getTemperature())
                .filter(temperature -> temperature >= 0 && temperature <= 2.0)
                .ifPresent(optionsBuilder::temperature);

        // 频率惩罚设置
        Optional.of(options.getFrequencyPenalty())
                .filter(frequencyPenalty -> frequencyPenalty >= -2.0 && frequencyPenalty <= 2.0)
                .ifPresent(optionsBuilder::frequencyPenalty);

        // 最大 token 设置
        Optional.ofNullable(options.getMaxTokens())
                .ifPresent(optionsBuilder::maxTokens);

        // top_p 设置
        Optional.of(options.getTopP())
                .filter(topP -> topP >= 0.1 && topP <= 1)
                .ifPresent(optionsBuilder::topP);

        return optionsBuilder;
    }
}
