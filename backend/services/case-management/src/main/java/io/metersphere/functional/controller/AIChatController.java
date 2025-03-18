package io.metersphere.functional.controller;

import io.metersphere.functional.request.ChatMessageDTO;
import io.metersphere.functional.service.AIPrivateAutoCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用例管理-自动生成用例")
@RestController
@RequestMapping("/case/chat")
public class AIChatController {

    @Resource
    private AIPrivateAutoCase aiPrivateAutoCase;

    @GetMapping("/add/case")
    @Operation(summary = "用例管理-自动生成json用例")
    public String chatCase(@RequestParam(value = "message", defaultValue = "What is the current time?") String message) {
        return aiPrivateAutoCase.chatCase(message);
    }

    @PostMapping("/analyze/demand")
    @Operation(summary = "用例管理-分析需求")
    public String chatAnalyzeDemand(@RequestParam(value = "message", defaultValue = "What is the current time?") String message, @RequestBody List<ChatMessageDTO> chatMessages) {
        return aiPrivateAutoCase.chatAnalyzeDemand(message, chatMessages);
    }

}
