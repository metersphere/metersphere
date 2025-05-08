package io.metersphere.ai.engine.holder;

import io.metersphere.ai.engine.common.AIChatClient;
import io.metersphere.ai.engine.common.AIChatOptions;
import io.metersphere.ai.engine.common.AIModelType;
import io.metersphere.ai.engine.common.AIRegister;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 一个管理并提供访问聊天客户端模型的类。
 * 该类扫描所有实现了 {@link AIChatClient} 的 Bean，并使用 {@link AIRegister} 注解中的键
 * 将它们注册到一个映射中，用于根据类型获取特定的聊天模型。
 */
@Component
public class ChatClientHolder {

    /**
     * 用于存储模型类型（来自 {@link AIRegister} 注解）和相应的 {@link AIChatClient} 实现类之间关联的映射。
     */
    private static final Map<String, AIChatClient> MODEL_MAP = new HashMap<>();

    /**
     * 构造函数，通过扫描应用上下文中的所有 {@link AIChatClient} 类型的 Bean，
     * 并使用 {@link AIRegister} 注解中的模型类型值将它们注册到 { MODEL_MAP } 中。
     *
     * @param context 提供访问所有 Bean 的 {@link ApplicationContext}。
     */
    public ChatClientHolder(ApplicationContext context) {
        // 获取所有实现了 AIChatClient 接口的 Bean
        Map<String, AIChatClient> beans = context.getBeansOfType(AIChatClient.class);

        // 遍历所有的 Bean 并根据 @AIRegister 注解获取 key
        for (Map.Entry<String, AIChatClient> entry : beans.entrySet()) {
            AIChatClient model = entry.getValue();
            AIRegister annotation = model.getClass().getAnnotation(AIRegister.class);

            if (annotation != null) {
                String modelType = annotation.value(); // 获取 @AIRegister 中的 value 作为 key
                MODEL_MAP.put(modelType, model); // 将 key 和对应的实现类添加到 MODEL_MAP 中
            }
        }
    }

    /**
     * 根据指定的模型类型获取相应的 {@link ChatClient}。
     * <p>
     * 该方法使用模型类型（即 {@link AIRegister} 注解中的键）来查找对应的 {@link AIChatClient}，
     * 然后获取并返回关联的 {@link ChatClient}。
     *
     * @param modelType 模型类型（来自 {@link AIRegister} 注解的值 {@link AIModelType} ）。
     * @param options   用于配置 {@link ChatClient} 的选项。
     * @return 与指定模型类型对应的 {@link ChatClient}。
     * @throws IllegalArgumentException 如果未找到与指定键对应的模型类型。
     */
    public static ChatClient getChatClient(String modelType, AIChatOptions options) {
        AIChatClient model = MODEL_MAP.get(modelType);
        if (model == null) {
            throw new IllegalArgumentException("不支持的聊天模型类型: " + modelType);
        }

        return model.chatClient(options);
    }
}
