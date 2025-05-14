package io.metersphere.api.ai;

import io.metersphere.ai.engine.common.AIChatOptions;
import io.metersphere.ai.engine.common.AIModelType;
import io.metersphere.ai.engine.tools.DateTimeTool;
import io.metersphere.ai.engine.tools.FileReaderTool;
import io.metersphere.ai.engine.tools.IntegrateTool;
import io.metersphere.ai.engine.tools.JvmTool;
import io.metersphere.sdk.util.LogUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.*;
import io.metersphere.ai.engine.ChatToolEngine;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class AiTest {

    private static final String OPENAI_API_KEY = "SK";
/*
    @Test
    @Order(1)
    void chatTest() {
        // 硅基流动免费 API
        String response = ChatToolEngine.builder(AIModelType.DEEP_SEEK,
                        AIChatOptions.builder()
                                .modelType("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B")
                                .apiKey(OPENAI_API_KEY)
                                .baseUrl("https://api.siliconflow.cn")
                                .build())
                .prompt("How are you?")
                .execute();

        System.out.println("Chat Response: " + response);
    }

    @Test
    @Order(2)
    void toolsTest() {
        String response = ChatToolEngine.builder(AIModelType.DEEP_SEEK,
                        AIChatOptions.builder()
                                .modelType("deepseek-chat")
                                .apiKey(OPENAI_API_KEY)
                                .baseUrl("https://api.deepseek.com")
                                .build())
                .addMemory("1", "明天时间是多少")
                .addMemory("1", "输出JVM 信息")
                .prompt("整合输出")
                .tools(List.of(new DateTimeTool(), new JvmTool(), new IntegrateTool()))
                .execute();


        LogUtils.info("Tools Response: {}", response);
    }

    @Test
    @Order(3)
    void toolsTest2() {
        ChatResponse response = ChatToolEngine.builder(AIModelType.OPEN_AI,
                        AIChatOptions.builder()
                                .modelType("gpt-3.5-turbo")
                                .apiKey(OPENAI_API_KEY)
                                .baseUrl("https://api.xty.app")
                                .build())
                .prompt("明天时间是多少")
                .tools(new DateTimeTool())
                .executeChatResponse();

        if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
            LogUtils.info("ToolsTest2 Response: {}", response.getResult().getOutput().getText());
        } else {
            LogUtils.info("ToolsTest2 Response is null or incomplete");
        }
    }

    @Test
    @Order(4)
    void toolsFilesTest() {
        ChatResponse response = ChatToolEngine.builder(AIModelType.DEEP_SEEK,
                        AIChatOptions.builder()
                                .modelType("deepseek-chat")
                                .apiKey(OPENAI_API_KEY)
                                .baseUrl("https://api.deepseek.com")
                                .build())
                .prompt("根据文件地址[/Users/zhaoyong/Downloads/package-lock.json] 内容分析 prettier 版本")
                .tools(new FileReaderTool())
                .executeChatResponse();

        if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
            LogUtils.info("ToolsFilesTest Response: {}", response.getResult().getOutput().getText());
        } else {
            LogUtils.info("ToolsFilesTest Response is null or incomplete");
        }
    }*/
}