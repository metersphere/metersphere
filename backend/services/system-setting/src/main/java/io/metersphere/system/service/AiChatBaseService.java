package io.metersphere.system.service;

import io.metersphere.ai.engine.ChatToolEngine;
import io.metersphere.ai.engine.common.AIChatOptions;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.AiConversationContent;
import io.metersphere.system.dto.request.ai.AIChatOption;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AdvSettingDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.mapper.AiConversationContentMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: jianxing
 * @CreateTime: 2025-05-28  13:44
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AiChatBaseService {

    @Resource
    MessageChatMemoryAdvisor messageChatMemoryAdvisor;
    @Resource
    private AiConversationContentMapper aiConversationContentMapper;

    public AiModelSourceDTO getModule(AIChatRequest request, String userId) {
        return Objects.requireNonNull(CommonBeanFactory.getBean(SystemAIConfigService.class))
                .getModelSourceDTOWithKey(request.getChatModelId(), userId);
    }

    /**
     * 聊天不记忆对话
     *
     * @param aiChatOption
     * @return
     */
    public ChatClient.CallResponseSpec chat(AIChatOption aiChatOption) {
        return getClient(aiChatOption.getModule())
                .prompt()
                .user(aiChatOption.getPrompt())
                .call();
    }

    /**
     * 聊天并记忆
     *
     * @param aiChatOption
     * @return
     */
    public ChatClient.CallResponseSpec chatWithMemory(AIChatOption aiChatOption) {
        if (StringUtils.isNotBlank(aiChatOption.getSystem())) {
            return getClient(aiChatOption.getModule())
                    .prompt()
                    .system(aiChatOption.getSystem())
                    .user(aiChatOption.getPrompt())
                    .advisors(messageChatMemoryAdvisor)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, aiChatOption.getConversationId()))
                    .call();
        }
        return getClient(aiChatOption.getModule())
                .prompt()
                .user(aiChatOption.getPrompt())
                .advisors(messageChatMemoryAdvisor)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, aiChatOption.getConversationId()))
                .call();
    }

    /**
     * 获取 AIChatClient
     *
     * @param model 模型配置
     * @return ChatClient
     */
    private ChatClient getClient(AiModelSourceDTO model) {
        return ChatToolEngine.builder(model.getProviderName(), getAiChatOptions(model))
                .getChatClient();
    }

    /**
     * 根据模型配置，获取 AIChatOptions
     *
     * @param model
     * @return
     */
    private AIChatOptions getAiChatOptions(AiModelSourceDTO model) {
        // 获取模块的高级设置参数
        Map<String, Object> paramMap = Optional.ofNullable(model.getAdvSettingDTOList())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(item -> StringUtils.isNotBlank(item.getName()) && BooleanUtils.isTrue(item.getEnable()))
                .collect(Collectors.toMap(
                        AdvSettingDTO::getName,
                        AdvSettingDTO::getValue,
                        (v1, v2) -> v2
                ));

        AIChatOptions aiChatOptions = JSON.parseObject(JSON.toJSONString(paramMap), AIChatOptions.class);

        // 设置模型信息
        aiChatOptions.setModelType(model.getBaseName());
        aiChatOptions.setApiKey(model.getAppKey());
        aiChatOptions.setBaseUrl(model.getApiUrl());
        return aiChatOptions;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AiConversationContent saveUserConversationContent(String conversationId, String content) {
        return saveConversationContent(conversationId, content, MessageType.USER.getValue());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AiConversationContent saveAssistantConversationContent(String conversationId, String content) {
        return saveConversationContent(conversationId, content, MessageType.ASSISTANT.getValue());
    }

    private AiConversationContent saveConversationContent(String conversationId, String content, String type) {
        AiConversationContent aiConversationContent = new AiConversationContent();
        aiConversationContent.setId(IDGenerator.nextStr());
        aiConversationContent.setConversationId(conversationId);
        aiConversationContent.setContent(content);
        aiConversationContent.setType(type);
        aiConversationContent.setCreateTime(System.currentTimeMillis());
        aiConversationContentMapper.insert(aiConversationContent);
        return aiConversationContent;
    }
}
