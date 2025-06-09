package io.metersphere.system.controller;

import io.metersphere.ai.engine.common.AIModelParamType;
import io.metersphere.ai.engine.common.AIModelType;
import io.metersphere.sdk.constants.InternalUser;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiConversation;
import io.metersphere.system.domain.AiConversationContent;
import io.metersphere.system.domain.AiConversationContentExample;
import io.metersphere.system.domain.AiModelSource;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AIConversationUpdateRequest;
import io.metersphere.system.dto.request.ai.AdvSettingDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.mapper.AiConversationContentMapper;
import io.metersphere.system.mapper.AiConversationMapper;
import io.metersphere.system.service.SystemAIConfigService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author: jianxing
 * @CreateTime: 2025-05-28  13:44
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AiConversationControllerTest extends BaseTest {

    public static final String BASE_PATH = "/ai/conversation/";
    public static final String ADD = "add";
    public static final String UPDATE = "update";
    public static final String LIST = "list";
    public static final String CHAT_LIST = "chat/list/{0}";
    public static final String CHAT = "chat";
    public static final String DELETE = "delete/{0}";

    private static String addAiConversationId = UUID.randomUUID().toString();
    private static AiModelSource module;

    @Value("${embedded.mockserver.host}")
    private String host;
    @Value("${embedded.mockserver.port}")
    private int port;

    @Resource
    private SystemAIConfigService systemAIConfigService;

    @Resource
    private AiConversationMapper aiConversationMapper;

    @Resource
    private AiConversationContentMapper aiConversationContentMapper;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(1)
    public void add() throws Exception {
        mockPost("/v1/chat/completions", """
                {
                    "id": "chatcmpl-B9MBs8CjcvOU2jLn4n570S5qMJKcT",
                    "object": "chat.completion",
                    "created": 1741569952,
                    "model": "gpt-4.1-2025-04-14",
                    "choices": [
                      {
                        "index": 0,
                        "message": {
                          "role": "assistant",
                          "content": "生成的用例",
                          "refusal": null,
                          "annotations": []
                        },
                        "logprobs": null,
                        "finish_reason": "stop"
                      }
                    ],
                    "usage": {
                      "prompt_tokens": 19,
                      "completion_tokens": 10,
                      "total_tokens": 29,
                      "prompt_tokens_details": {
                        "cached_tokens": 0,
                        "audio_tokens": 0
                      },
                      "completion_tokens_details": {
                        "reasoning_tokens": 0,
                        "audio_tokens": 0,
                        "accepted_prediction_tokens": 0,
                        "rejected_prediction_tokens": 0
                      }
                    },
                    "service_tier": "default"
                }        
                """);

        initModule();

        AIChatRequest request = new AIChatRequest();
        request.setPrompt("你好");
        request.setConversationId(addAiConversationId);
        request.setChatModelId(module.getId());
        request.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        this.requestPostWithOkAndReturn(ADD, request);
        AiConversation aiConversation = aiConversationMapper.selectByPrimaryKey(addAiConversationId);
        Assertions.assertEquals(aiConversation.getTitle(), "生成的用例");
        Assertions.assertEquals(aiConversation.getCreateUser(), InternalUser.ADMIN.getValue());
    }

    private void initModule() {
        AiModelSourceDTO aiModelSourceDTO = new AiModelSourceDTO();
        aiModelSourceDTO.setName(UUID.randomUUID().toString());
        aiModelSourceDTO.setType("LLM");
        aiModelSourceDTO.setProviderName(AIModelType.DEEP_SEEK);
        aiModelSourceDTO.setPermissionType(AIConfigConstants.AiPermissionType.PRIVATE.toString());
        aiModelSourceDTO.setOwnerType(AIConfigConstants.AiOwnerType.PERSONAL.toString());
        aiModelSourceDTO.setBaseName("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
        aiModelSourceDTO.setApiUrl("http://" + host + ":" + port);
        aiModelSourceDTO.setAppKey("sk-ryyuiioommnn");
        aiModelSourceDTO.setStatus(true);

        AdvSettingDTO advSettingDTO = new AdvSettingDTO();
        advSettingDTO.setName(AIModelParamType.TEMPERATURE);
        advSettingDTO.setLabel("温度");
        advSettingDTO.setValue(0.7);
        advSettingDTO.setEnable(false);
        List<AdvSettingDTO> list = new ArrayList<>();
        list.add(advSettingDTO);
        aiModelSourceDTO.setAdvSettingDTOList(list);
        module = systemAIConfigService.editModuleConfig(aiModelSourceDTO, InternalUser.ADMIN.getValue());
    }

    @Test
    @Order(1)
    public void chat() throws Exception {
        AIChatRequest request = new AIChatRequest();
        request.setPrompt("生成用例");
        request.setConversationId(UUID.randomUUID().toString());
        request.setChatModelId(module.getId());
        request.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(CHAT, request);
        String resultData = getResultData(mvcResult, String.class);
        Assertions.assertEquals(resultData, "生成的用例");

        // 手动添加对话内容，真实场景由spring-ai处理
        AiConversationContent aiConversationContent = new AiConversationContent();
        aiConversationContent.setId(IDGenerator.nextStr());
        aiConversationContent.setConversationId(addAiConversationId);
        aiConversationContent.setType(MessageType.USER.getValue());
        aiConversationContent.setCreateTime(System.currentTimeMillis());
        aiConversationContent.setContent("生成用例");
        aiConversationContentMapper.insert(aiConversationContent);
    }

    @Test
    @Order(2)
    public void updateTitle() throws Exception {
        AIConversationUpdateRequest request = new AIConversationUpdateRequest();
        request.setId(addAiConversationId);
        request.setTitle(UUID.randomUUID().toString());

        MvcResult mvcResult = this.requestPostWithOkAndReturn(UPDATE, request);
        AiConversation resultData = getResultData(mvcResult, AiConversation.class);
        Assertions.assertEquals(resultData.getTitle(), request.getTitle());
    }

    @Test
    @Order(2)
    public void list() throws Exception {
        this.requestGetWithOkAndReturn(LIST);
    }

    @Test
    @Order(3)
    public void chatList() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(CHAT_LIST, addAiConversationId);
        List<AiConversationContent> resultDataArray = getResultDataArray(mvcResult, AiConversationContent.class);
        Assertions.assertFalse(resultDataArray.isEmpty());
        Assertions.assertNotNull(resultDataArray.get(0).getContent());
    }

    @Test
    @Order(10)
    public void delete() throws Exception {
        this.requestGetWithOkAndReturn(DELETE, addAiConversationId);

        AiConversation aiConversation = aiConversationMapper.selectByPrimaryKey(addAiConversationId);
        Assertions.assertNull(aiConversation);

        AiConversationContentExample example = new AiConversationContentExample();
        example.createCriteria().andConversationIdEqualTo(addAiConversationId);
        List<AiConversationContent> aiConversationContents = aiConversationContentMapper.selectByExample(example);
        Assertions.assertTrue(aiConversationContents.isEmpty());
    }
}
