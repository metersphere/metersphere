package io.metersphere.ai.engine;

import io.metersphere.ai.engine.common.AIChatOptions;
import io.metersphere.ai.engine.holder.ChatClientHolder;
import io.metersphere.sdk.util.LogUtils;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * ChatToolEngine 用于处理带有工具支持的聊天请求。
 * <p>
 * 该引擎支持记忆功能、工具辅助以及流式和结构化响应的执行方式。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * ChatResponse response = ChatToolEngine.builder(AIModelType.OPEN_AI,
 *     AiChatOptions.builder()
 *         .modelType("gpt-3.5-turbo")
 *         .apiKey("sk-xxx")
 *         .baseUrl("url")
 *         .build())
 *     .addPrompt("明天时间是多少")
 *     .tools(new DateTimeTool())
 *     .executeChatResponse();
 * </pre>
 * </p>
 */

public class ChatToolEngine {

    /**
     * 创建一个 ChatToolEngine.Builder 实例，用于构造带工具支持的聊天请求。
     *
     * @param modelType 模型类型
     * @param options   聊天选项
     * @return Builder 实例
     */
    public static Builder builder(String modelType, AIChatOptions options) {
        return new Builder(modelType, options);
    }

    /**
     * Builder 内部类用于构造和执行带工具支持的聊天请求。
     */
    public static class Builder {
        /**
         * 聊天记忆对象，用于存储对话历史记录。
         */
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();
        /**
         * 聊天客户端实例。
         */
        @Getter
        private final ChatClient chatClient;
        /**
         * 工具列表。
         */
        private final List<Object> tools = new LinkedList<>();
        /**
         * 聊天提示信息。
         */
        private String prompt;
        /**
         * 系统角色
         */
        private String system;

        /**
         * 私有构造函数，通过模型类型和选项初始化聊天客户端。
         *
         * @param modelType 模型类型
         * @param options   聊天选项
         */
        private Builder(String modelType, AIChatOptions options) {
            this.chatClient = ChatClientHolder.getChatClient(modelType, options);
        }

        /**
         * 向聊天记忆中添加一条消息记录。
         *
         * @param conversationId 对话 ID
         * @param msg            消息内容
         * @return 当前 Builder 实例，用于链式调用
         */
        public Builder addMemory(String conversationId, String msg) {
            this.chatMemory.add(conversationId, new UserMessage(msg));
            return this;
        }

        /**
         * 添加工具支持，支持传入多个工具对象。
         *
         * @param tools 工具数组
         * @return 当前 Builder 实例，用于链式调用
         */
        public Builder tools(Object... tools) {
            this.tools.addAll(List.of(tools));
            return this;
        }

        /**
         * 设置聊天请求的提示信息。
         *
         * @param prompt 聊天提示信息
         * @return 当前 Builder 实例，用于链式调用
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * 设置系统角色。
         *
         * @param system 系统角色
         * @return 当前 Builder 实例，用于链式调用
         */
        public Builder system(String system) {
            this.system = system;
            return this;
        }

        /**
         * 执行聊天请求，并以字符串形式返回响应内容。
         *
         * @return 聊天响应内容
         */
        public String execute() {
            LogUtils.info("Processing chat request for messages: {}", chatMemory);
            return buildRequest()
                    .call()
                    .content();
        }

        /**
         * 执行聊天请求，返回 {@link ChatResponse} 对象，
         * 包含详细的响应信息和元数据。
         *
         * @return 聊天响应对象
         */
        public ChatResponse executeChatResponse() {
            LogUtils.info("Starting chat response for messages: {}", chatMemory);
            return buildRequest()
                    .call()
                    .chatResponse();
        }

        /**
         * 执行聊天流请求，返回 Flux 流，适用于响应数据流式输出的场景。
         *
         * @return 聊天响应内容流
         */
        public Flux<String> executeStream() {
            LogUtils.info("Starting chat stream for messages: {}", chatMemory);
            return buildRequest()
                    .stream()
                    .content();
        }

        /**
         * 执行结构化聊天请求，并将响应转换为指定的数据结构。
         *
         * @param clazz        期望的响应数据类型
         * @param <T>          响应数据的泛型类型
         * @return 结构化的聊天响应
         */
        public <T> T executeStructured(Class<T> clazz) {
            LogUtils.info("Processing structured chat request for messages: {}", prompt);
            return buildRequest()
                    .call()
                    .entity(clazz);
        }

        /**
         * 执行结构化聊天请求，并将响应转换为指定的数据结构。
         *
         * @param responseType 期望的响应数据类型引用
         * @param <T>          响应数据的泛型类型
         * @return 结构化的聊天响应
         */
        public <T> T executeStructured(ParameterizedTypeReference<T> responseType) {
            LogUtils.info("Processing structured chat request for messages: {}", prompt);
            return buildRequest()
                    .call()
                    .entity(responseType);
        }

        /**
         * 用于构建ChatClient请求规约的建造者类。
         * <p>
         * 本类采用流畅接口设计模式，支持链式调用方式配置请求参数，并对输入参数进行有效性校验。
         * </p>
         *
         * @see ChatClient.ChatClientRequestSpec
         */
        private class RequestBuilder {
            private ChatMemory chatMemory;
            private String system;
            private String prompt;
            private List<Object> tools;

            /**
             * 配置聊天记忆上下文
             *
             * @param memory 聊天记忆实例，记录历史对话信息。允许为null值，
             *               表示不启用记忆功能
             * @return 当前建造者实例，支持链式调用
             */
            public RequestBuilder withChatMemory(ChatMemory memory) {
                this.chatMemory = memory;
                return this;
            }

            /**
             * 设置系统级提示消息
             * <p>
             * 当传入值为null或空白字符串时自动忽略该配置项
             * </p>
             *
             * @param system 系统消息内容，用于定义AI的基础行为模式
             * @return 当前建造者实例，支持链式调用
             */
            public RequestBuilder withSystemMessage(String system) {
                if (system != null && !system.isBlank()) {
                    this.system = system;
                }
                return this;
            }

            /**
             * 设置用户输入的提示内容
             *
             * @param prompt 用户输入的提示文本，不可为空
             * @return 当前建造者实例，支持链式调用
             * @throws IllegalArgumentException 当提示内容为空或仅包含空白字符时抛出
             */
            public RequestBuilder withUserPrompt(String prompt) {
                if (prompt == null || prompt.isBlank()) {
                    throw new IllegalArgumentException("Prompt cannot be empty");
                }
                this.prompt = prompt;
                return this;
            }

            /**
             * 配置要启用的工具集合
             * <p>
             * 当传入空列表或null值时自动忽略该配置项
             * </p>
             *
             * @param tools 工具对象列表，用于扩展AI的功能能力
             * @return 当前建造者实例，支持链式调用
             */
            public RequestBuilder withTools(List<Object> tools) {
                if (tools != null && !tools.isEmpty()) {
                    this.tools = new ArrayList<>(tools);
                }
                return this;
            }

            /**
             * 构建最终的请求规约对象
             * <p>
             * 根据已配置的参数按以下顺序构建请求：
             * <ol>
             *   <li>若配置了聊天记忆，添加记忆顾问</li>
             *   <li>若存在系统消息，添加系统级提示</li>
             *   <li>添加必须的用户提示</li>
             *   <li>若配置了工具列表，启用指定工具</li>
             * </ol>
             * </p>
             *
             * @return 配置完成的ChatClient请求规约实例
             */
            public ChatClient.ChatClientRequestSpec build() {
                ChatClient.ChatClientRequestSpec request = chatClient.prompt();

                if (chatMemory != null) {
                    request = request.advisors(PromptChatMemoryAdvisor.builder(chatMemory).build());
                }

                if (system != null) {
                    request = request.system(system);
                }

                request = request.user(prompt);

                if (tools != null) {
                    request = request.tools(tools.toArray());
                }

                return request;
            }
        }

        /**
         * 构建基础请求规约对象
         * <p>
         * 在构建过程中会进行以下校验：
         * <ul>
         *   <li>检查用户提示是否已正确设置</li>
         *   <li>验证所有参数的有效性</li>
         * </ul>
         * </p>
         *
         * @return 配置完成的ChatClient请求规约实例
         * @throws IllegalStateException 当未设置用户提示时抛出
         */
        private ChatClient.ChatClientRequestSpec buildRequest() {
            if (prompt == null || prompt.isEmpty()) {
                throw new IllegalStateException("Prompt must be set before execution.");
            }

            return new RequestBuilder()
                    .withChatMemory(chatMemory)
                    .withSystemMessage(system)
                    .withUserPrompt(prompt)
                    .withTools(tools)
                    .build();
        }
    }
}
