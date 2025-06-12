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

import java.util.HashMap;

/**
 * 该类实现了 AIChatModel 接口，用于集成 OpenAI 的聊天 API。
 * 提供了使用 OpenAI 的 API 和选项创建 ChatClient 和 ChatModel 的方法。
 * 该类被注解为 Spring 的组件，并注册为 OPEN_AI 模型类型。
 */
@AIRegister(AIModelType.OPEN_AI)
@Component
public class AIOpenAIChatClient extends AIChatClient {

    /**
     * 使用提供的选项创建一个 ChatClient 实例。
     *
     * @param options 包含 API 密钥、模型类型及其他设置的选项。
     * @return 配置好的 ChatClient 实例。
     */
    @Override
    public ChatClient chatClient(AIChatOptions options) {
        ChatClient.Builder builder = ChatClient.builder(chatModel(options));

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
        // 创建 OpenAiApi 实例
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(options.getApiKey())
                .baseUrl(options.getBaseUrl())
                .build();

        // 创建 OpenAiChatModel 实例并配置
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept-Encoding", "gzip, deflate");
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().httpHeaders(headerMap)
                        .model(options.getModelType())
                        .build())
                .build();
    }

}
