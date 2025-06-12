package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.domain.AiConversation;
import io.metersphere.system.domain.AiConversationContent;
import io.metersphere.system.domain.AiConversationContentExample;
import io.metersphere.system.domain.AiConversationExample;
import io.metersphere.system.dto.request.ai.AIChatOption;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AIConversationUpdateRequest;
import io.metersphere.system.mapper.AiConversationContentMapper;
import io.metersphere.system.mapper.AiConversationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2025-05-28  13:44
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AiConversationService {

    @Resource
    AiChatBaseService aiChatBaseService;
    @Resource
    AiConversationMapper aiConversationMapper;
    @Resource
    AiConversationContentMapper aiConversationContentMapper;

    public String chat(AIChatRequest request, String userId) {
        // 持久化原始提示词
        aiChatBaseService.saveUserConversationContent(request.getConversationId(), request.getPrompt());

        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(aiChatBaseService.getModule(request, userId))
                .prompt(request.getPrompt())
                .build();
        String assistantMessage = aiChatBaseService.chatWithMemory(aiChatOption)
                .content();

        // 持久化回答内容
        aiChatBaseService.saveAssistantConversationContent(request.getConversationId(), assistantMessage);
        return assistantMessage;
    }

    public AiConversation add(AIChatRequest request, String userId) {
        String prompt = """
                概况用户输入的主旨生成本轮对话的标题，只返回标题，不带标点符号，最好50字以内，不超过255。
                用户输入:
                """ + request.getPrompt();
        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(aiChatBaseService.getModule(request, userId))
                .prompt(prompt)
                .build();

        String conversationTitle = request.getPrompt();
        try {
            conversationTitle = aiChatBaseService.chat(aiChatOption)
                    .content();
            conversationTitle = conversationTitle.replace("\"", "");
        } catch (Exception e) {
            LogUtils.error(e);
        }

        if (conversationTitle.length() > 255) {
            conversationTitle = conversationTitle.substring(0, 255);
        }
        AiConversation aiConversation = new AiConversation();
        aiConversation.setId(request.getConversationId());
        aiConversation.setTitle(conversationTitle);
        aiConversation.setCreateUser(userId);
        aiConversation.setCreateTime(System.currentTimeMillis());
        aiConversationMapper.insert(aiConversation);
        return aiConversation;
    }

    public void delete(String conversationId, String userId) {
        AiConversation aiConversation = aiConversationMapper.selectByPrimaryKey(conversationId);
        checkConversationPermission(userId, aiConversation);
        aiConversationMapper.deleteByPrimaryKey(conversationId);

        AiConversationContentExample example = new AiConversationContentExample();
        example.createCriteria().andConversationIdEqualTo(conversationId);
        aiConversationContentMapper.deleteByExample(example);
    }

    private void checkConversationPermission(String userId, AiConversation aiConversation) {
        if (aiConversation == null) {
            throw new MSException(MsHttpResultCode.NOT_FOUND);
        }
        if (!StringUtils.equals(aiConversation.getCreateUser(), userId)) {
            throw new MSException(MsHttpResultCode.FORBIDDEN);
        }
    }

    public List<AiConversation> list(String userId) {
        AiConversationExample example = new AiConversationExample();
        example.createCriteria().andCreateUserEqualTo(userId);
        return aiConversationMapper.selectByExample(example).reversed();
    }

    public List<AiConversationContent> chatList(String conversationId, String userId) {
        AiConversation aiConversation = aiConversationMapper.selectByPrimaryKey(conversationId);
        checkConversationPermission(userId, aiConversation);
        AiConversationContentExample example = new AiConversationContentExample();
        example.createCriteria().andConversationIdEqualTo(conversationId);
        example.setOrderByClause("create_time");
        return aiConversationContentMapper.selectByExampleWithBLOBs(example);
    }

    public AiConversation update(AIConversationUpdateRequest request, String userId) {
        AiConversation originConversation = aiConversationMapper.selectByPrimaryKey(request.getId());
        checkConversationPermission(userId, originConversation);
        AiConversation aiConversation = BeanUtils.copyBean(new AiConversation(), request);
        aiConversationMapper.updateByPrimaryKeySelective(aiConversation);
        originConversation.setTitle(aiConversation.getTitle());
        return originConversation;
    }
}
