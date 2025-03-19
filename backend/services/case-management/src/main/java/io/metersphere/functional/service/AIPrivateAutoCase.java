package io.metersphere.functional.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import io.metersphere.functional.request.ChatMessageDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;

/**
 * @author guoyuqi
 * @date 2025-03-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AIPrivateAutoCase {

    static String MODEL_NAME = "qwen2.5:7b"; // try other local ollama model names
    static String BASE_URL = "https://deepseek.wata.site"; // local ollama base url

    /*static String MODEL_NAME = "deepseek-r1:1.5b"; // try other local ollama model names
    static String BASE_URL = "https://deepseek.wata.site";*/

    public String chatCase(String message) {
        /*OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
        return model.chat(message);*/
        //return model.chat(message);
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .responseFormat(JSON)
                .build();

        List<ChatMessage> messages = List.of(
                SystemMessage.from("你是一个ai助手，可以根据需求生成用例，包括用例名称，前置条件，步骤描述，预期结果，返回的用例数据为json格式"),
                UserMessage.from(message));

        //qwen
        ChatResponse chatResponse = model.chat(ChatRequest.builder()
                .messages(messages)
                .responseFormat(ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(JsonSchema.builder().rootElement(JsonObjectSchema.builder()
                                .addProperty("testCases", JsonArraySchema.builder()
                                        .items(JsonObjectSchema.builder()
                                                .addProperty("testCaseName", JsonStringSchema.builder().build())
                                                .addProperty("prerequisite",JsonStringSchema.builder().build())
                                                .addProperty("stepDescription",JsonStringSchema.builder().build())
                                                .addProperty("expectedResult",JsonStringSchema.builder().build())
                                                .required("testCaseName", "prerequisite", "stepDescription", "expectedResult")
                                                .build()).build())
                                        .build())
                                .build())
                                .build()).build());
        return chatResponse.aiMessage().text();

        //deepseek
        /*ChatResponse chatResponse = model.chat(ChatRequest.builder()
                .messages(messages).build());*/
        /*String text = chatResponse.aiMessage().text();
        int start = text.indexOf("```json");

        int end = text.lastIndexOf("```");*/
        //String chat = model.chat(message);
        //boolean contains = chat.contains("```json");
        //return text;
    }
    interface Assistant {

        String chat(String message);
    }

    public String chatAnalyzeDemand(String message, List<ChatMessageDTO>chatMessages) {

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        List<ChatMessageDTO> analyze = chatMessages.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getType(), "ANALYZE")).toList();
        OpenAiChatModel model = OpenAiChatModel.builder()
                .modelName("Qwen/QwQ-32B")
                .build();

        chatMemory.add(systemMessage("你是产品需求分析专家，你可以判读需求的目的、范围、定义、功能是否完善，要求返回数据中包含completeness属性，即需求的完整度的百分比，和suggestion属性，即对于完善需求提出的修改建议"));

        for (ChatMessageDTO chatMessageDTO : analyze) {
            if (chatMessageDTO.isSelf()) {
                UserMessage userMessage = new UserMessage(chatMessageDTO.getText());
                chatMemory.add(userMessage);
            }
            else {
                AiMessage aiMessage = new AiMessage(chatMessageDTO.getText());
                chatMemory.add(aiMessage);
            }
        }
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemory(chatMemory)
                .build();

       return assistant.chat(message);
    }
}
