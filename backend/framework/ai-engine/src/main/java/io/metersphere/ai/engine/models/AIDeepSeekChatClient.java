package io.metersphere.ai.engine.models;

import io.metersphere.ai.engine.common.AIChatClient;
import io.metersphere.ai.engine.common.AIChatOptions;
import io.metersphere.ai.engine.common.AIModelType;
import io.metersphere.ai.engine.common.AIRegister;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

/**
 * 该类实现了 AIChatModel 接口，并与 DeepSeek 模型集成。
 * 通过 OpenAI API 创建 ChatClient 和 ChatModel，便于与 DeepSeek 进行交互。
 * 该类被注解为 Spring 的组件，并注册为 DEEP_SEEK 模型类型。
 */
@AIRegister(AIModelType.DEEP_SEEK)
@Component
public class AIDeepSeekChatClient extends AIChatClient {

    /**
     * 创建 OpenAiApi 实例，用于与 OpenAI API 进行交互。
     *
     * @param options 包含 API 密钥、基础 URL 和其他设置的选项。
     * @return 配置好的 OpenAiApi 实例。
     */
    private OpenAiApi createOpenAiApi(AIChatOptions options) {
        return OpenAiApi.builder()
                .apiKey(options.getApiKey())
                .baseUrl(options.getBaseUrl())
                .build();
    }

    /**
     * 创建 OpenAiChatModel 实例，用于与 OpenAI API 进行聊天交互。
     *
     * @param options   包含模型类型等选项的对象。
     * @param openAiApi 创建好的 OpenAiApi 实例。
     * @return 配置好的 OpenAiChatModel 实例。
     */
    private OpenAiChatModel createChatModel(AIChatOptions options, OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(options.getModelType())
                        .build())
                .build();
    }

    /**
     * 使用提供的选项创建一个 ChatClient 实例。
     *
     * @param options 包含 API 密钥、模型类型及其他设置的选项。
     * @return 配置好的 ChatClient 实例。
     */
    @Override
    public ChatClient chatClient(AIChatOptions options) {
        // 创建 OpenAiApi 和 ChatModel 实例
        OpenAiApi openAiApi = createOpenAiApi(options);
        OpenAiChatModel chatModel = createChatModel(options, openAiApi);

        // 构建 ChatClient.Builder 实例
        ChatClient.Builder builder = ChatClient.builder(chatModel);

        // 添加默认顾问
        this.addAdvisor(options, builder);

        // 将构建好的选项应用到 ChatClient.Builder 中
        builder.defaultOptions(this.builderChatOptions(options).build());

        return builder.build();
    }

    /**
     * 创建一个 ChatModel 实例，用于与 OpenAI API 进行通信。
     *
     * @param options 包含 API 密钥、基础 URL、模型类型等选项的对象。
     * @return 配置好的 ChatModel 实例。
     */
    @Override
    public ChatModel chatModel(AIChatOptions options) {
        // 创建并返回 ChatModel 实例
        return createChatModel(options, createOpenAiApi(options));
    }
}
