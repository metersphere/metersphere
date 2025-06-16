package io.metersphere.system.config.ai;

import io.metersphere.system.domain.AiConversationContent;
import io.metersphere.system.mapper.ExtAiConversationContentMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用 MessageWindowChatMemory 只能记忆和持久化 max_messages 条消息
 * 该自定义类，能持久化所有消息，并且设置 ai 记忆消息的条数
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class MsMessageChatMemory implements ChatMemory {

    /**
     * 默认记忆10条消息
     */
    private static final int DEFAULT_MAX_MESSAGES = 10;


    @Resource
    private ExtAiConversationContentMapper extAiConversationContentMapper;

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 这里不处理，手动保存原始的提示词
    }

    @Override
    public List<Message> get(String conversationId) {
        // 获取最近的几条聊天，进行记忆
        List<AiConversationContent> contents = extAiConversationContentMapper.selectLastByConversationIdByLimit(conversationId, DEFAULT_MAX_MESSAGES)
                .reversed();

        // 先持久化了提示词，会重复，这里去掉最后一条
        contents.removeLast();

        return contents.stream()
                .map(conversationContent -> {
                    MessageType type = MessageType.fromValue(conversationContent.getType());
                    String content = conversationContent.getContent();
                    Message message = switch (type) {
                        case USER -> new UserMessage(content);
                        case ASSISTANT -> new AssistantMessage(content);
                        case SYSTEM -> new SystemMessage(content);
                        // The content is always stored empty for ToolResponseMessages.
                        // If we want to capture the actual content, we need to extend
                        // AddBatchPreparedStatement to support it.
                        case TOOL -> new ToolResponseMessage(List.of());
                    };
                    return message;
                }).collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        // do nothing
    }
}
