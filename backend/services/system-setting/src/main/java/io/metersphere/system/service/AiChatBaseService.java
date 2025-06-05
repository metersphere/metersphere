package io.metersphere.system.service;

import io.metersphere.ai.engine.ChatToolEngine;
import io.metersphere.ai.engine.common.AIChatOptions;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: jianxing
 * @CreateTime: 2025-05-28  13:44
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AiChatBaseService {

    @Resource
    MessageChatMemoryAdvisor messageChatMemoryAdvisor;

    public ChatClient.CallResponseSpec chat(String prompt, AiModelSourceDTO module) {
        return getClient(module)
                .prompt()
                .user(prompt)
                .call();
    }

    public AiModelSourceDTO getModule(AIChatRequest request, String userId) {
        return Objects.requireNonNull(CommonBeanFactory.getBean(SystemAIConfigService.class))
                .getModelSourceDTO(request.getChatModelId(), userId, request.getOrganizationId());
    }

    public ChatClient.CallResponseSpec chatWithMemory(AIChatRequest request, AiModelSourceDTO module) {
        return getClient(module)
                .prompt()
                .user(request.getPrompt())
                .advisors(messageChatMemoryAdvisor)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
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
        Map<String, Object> paramMap = new HashMap<>();
        model.getAdvSettingDTOList().stream()
                .filter(item -> StringUtils.isNotBlank(item.getName()))
                .forEach(item -> paramMap.put(item.getName(), item.getValue()));
        AIChatOptions aiChatOptions = JSON.parseObject(JSON.toJSONString(paramMap), AIChatOptions.class);

        // 设置模型信息
        aiChatOptions.setModelType(model.getBaseName());
        aiChatOptions.setApiKey(model.getAppKey());
        aiChatOptions.setBaseUrl(model.getApiUrl());
        return aiChatOptions;
    }
}
