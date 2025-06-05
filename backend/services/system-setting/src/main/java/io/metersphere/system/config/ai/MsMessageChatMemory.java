package io.metersphere.system.config.ai;

import io.metersphere.system.domain.AiConversationContent;
import io.metersphere.system.mapper.AiConversationContentMapper;
import io.metersphere.system.mapper.ExtAiConversationContentMapper;
import io.metersphere.system.uid.IDGenerator;
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
    private AiConversationContentMapper aiConversationContentMapper;
    @Resource
    private ExtAiConversationContentMapper extAiConversationContentMapper;

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 插入当前消息
        List<AiConversationContent> contents = messages.stream()
                .map(message -> {
                    AiConversationContent aiConversationContent = new AiConversationContent();
                    aiConversationContent.setId(IDGenerator.nextStr());
                    aiConversationContent.setConversationId(conversationId);
                    aiConversationContent.setContent(message.getText());
                    aiConversationContent.setType(message.getMessageType().getValue());
                    aiConversationContent.setCreateTime(System.currentTimeMillis());
                    return aiConversationContent;
                })
                .toList();
        aiConversationContentMapper.batchInsert(contents);
    }

    @Override
    public List<Message> get(String conversationId) {
        // 获取最近的几条聊天，进行记忆
        List<AiConversationContent> contents = extAiConversationContentMapper.selectLastByConversationIdByLimit(conversationId, DEFAULT_MAX_MESSAGES)
                .reversed();

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
