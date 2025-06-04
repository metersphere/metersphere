package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.domain.AiConversation;
import io.metersphere.system.domain.AiConversationContent;
import io.metersphere.system.domain.AiConversationContentExample;
import io.metersphere.system.domain.AiConversationExample;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AIConversationUpdateRequest;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.mapper.AiConversationContentMapper;
import io.metersphere.system.mapper.AiConversationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2025-05-28  13:44
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AiConversationService {

    @Resource
    AiChatBaseService aiChatBaseService;
    @Resource
    AiConversationMapper aiConversationMapper;
    @Resource
    AiConversationContentMapper aiConversationContentMapper;

    public String chat(AIChatRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        return aiChatBaseService.chatWithMemory(request, module)
                .content();
    }

    public AiConversation add(AIChatRequest request, String userId) {
        String prompt = "请用简短的文字概况以下内容的主旨，字数不超过225：\n" + request.getPrompt();
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        String conversationTitle = aiChatBaseService.chat(prompt, module).content();
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
        return aiConversationMapper.selectByExample(example);
    }

    public List<AiConversationContent> chatList(String conversationId, String userId) {
        AiConversation aiConversation = aiConversationMapper.selectByPrimaryKey(conversationId);
        checkConversationPermission(userId, aiConversation);
        AiConversationContentExample example = new AiConversationContentExample();
        example.createCriteria().andConversationIdEqualTo(conversationId);
        example.setOrderByClause("timestamp DESC");
        return aiConversationContentMapper.selectByExample(example);
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
