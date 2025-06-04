package io.metersphere.system.controller;

import io.metersphere.system.domain.AiConversation;
import io.metersphere.system.domain.AiConversationContent;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AIConversationUpdateRequest;
import io.metersphere.system.service.AiConversationService;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2025-05-28  13:44
 */
@Tag(name = "AI对话")
@RestController
@RequestMapping(value = "/ai/conversation")
public class AiConversationController {

    @Resource
    private AiConversationService aiConversationService;

    @GetMapping(value = "/list")
    @Operation(summary = "对话列表")
    public List<AiConversation> list() {
        return aiConversationService.list(SessionUtils.getUserId());
    }

    @GetMapping(value = "/chat/list/{conversationId}")
    @Operation(summary = "对话内容列表")
    public List<AiConversationContent> chatList(@PathVariable String conversationId) {
        return aiConversationService.chatList(conversationId, SessionUtils.getUserId());
    }

    @PostMapping(value = "/add")
    @Operation(summary = "添加对话")
    public AiConversation add(@Validated @RequestBody AIChatRequest request) {
        return aiConversationService.add(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "修改对话标题")
    public AiConversation add(@Validated @RequestBody AIConversationUpdateRequest request) {
        return aiConversationService.update(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/chat")
    @Operation(summary = "聊天")
    public String chat(@Validated @RequestBody AIChatRequest request) {
        return aiConversationService.chat(request, SessionUtils.getUserId());
    }

    @GetMapping(value = "/delete/{conversationId}")
    @Operation(summary = "删除对话")
    public void delete(@PathVariable String conversationId) {
        aiConversationService.delete(conversationId, SessionUtils.getUserId());
    }
}
