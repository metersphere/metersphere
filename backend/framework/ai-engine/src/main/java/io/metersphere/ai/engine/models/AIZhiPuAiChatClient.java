package io.metersphere.ai.engine.models;

import io.metersphere.ai.engine.common.AIChatClient;
import io.metersphere.ai.engine.common.AIChatOptions;
import io.metersphere.ai.engine.common.AIModelType;
import io.metersphere.ai.engine.common.AIRegister;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.stereotype.Component;

/**
 * CrmZhiPuAiChatModel 类实现了 {@link AIChatClient} 接口，
 * 用于构建基于知蒲AI的聊天模型及其对应的聊天客户端。
 * <p>
 * 通过 {@link AIRegister} 注解注册，模型类型为 {@link AIModelType#ZHI_PU_AI}。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 *     CrmZhiPuAiChatModel model = new CrmZhiPuAiChatModel();
 *     ChatClient client = model.chatClient(options);
 * </pre>
 * </p>
 *
 * @see AIChatClient
 */
@AIRegister(AIModelType.ZHI_PU_AI)
@Component
public class AIZhiPuAiChatClient extends AIChatClient {

    /**
     * 创建并返回基于知蒲AI的 {@link ChatClient} 实例。
     *
     * @param options 聊天选项，包括 API Key、Base URL 以及模型类型等信息
     * @return 构建好的 ChatClient 实例
     */
    @Override
    public ChatClient chatClient(AIChatOptions options) {
        ChatClient.Builder builder = ChatClient.builder(chatModel(options));

        // 添加默认顾问
        this.addAdvisor(options, builder);

        // 将构建好的选项应用到 ChatClient.Builder 中
        builder.defaultOptions(ZhiPuAiChatOptions.builder()
                .topP(options.getTopP())
                .temperature(options.getTemperature())
                .maxTokens(options.getMaxTokens()).build());
        return builder.build();
    }

    /**
     * 创建并返回基于知蒲AI的 {@link ChatModel} 实例。
     *
     * @param options 聊天选项，包括 API Key、Base URL 以及模型类型等信息
     * @return 构建好的 ChatModel 实例
     */
    @Override
    public ChatModel chatModel(AIChatOptions options) {
        var zhiPuAiApi = new ZhiPuAiApi(options.getBaseUrl(), options.getApiKey());
        return new ZhiPuAiChatModel(zhiPuAiApi, ZhiPuAiChatOptions.builder()
                .model(options.getModelType())
                .build());
    }
}
