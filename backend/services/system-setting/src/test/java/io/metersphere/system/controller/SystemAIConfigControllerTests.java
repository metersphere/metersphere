package io.metersphere.system.controller;

import io.metersphere.ai.engine.common.AIModelParamType;
import io.metersphere.ai.engine.common.AIModelType;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.dto.request.ai.AdvSettingDTO;
import io.metersphere.system.dto.request.ai.ModelSourceDTO;
import io.metersphere.system.dto.request.ai.ModelSourceRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemAIConfigControllerTests  extends BaseTest {
    @Resource
    private MockMvc mockMvc;

    public static final String BASE_PATH = "/ai/config";
    public static final String EDIT_SOURCE = "/edit-source";
    public static final String GET_LIST = "/source/list";
    public static final String DETAIL = "/get/";

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void testEdit() throws Exception {
        ModelSourceDTO modelSourceDTO = new ModelSourceDTO();
        modelSourceDTO.setName("测试模型");
        modelSourceDTO.setType("LLM");
        modelSourceDTO.setAvatar("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAPYA…9HyMkoW0e16yd+t8gdf0PxNHdl2KDVEMAAAAASUVORK5CYII=");
        modelSourceDTO.setProviderName(AIModelType.DEEP_SEEK);
        modelSourceDTO.setPermissionType(AIConfigConstants.AiPermissionType.PUBLIC.toString());
        modelSourceDTO.setOwnerType(AIConfigConstants.AiOwnerType.ORGANIZATION.toString());
        modelSourceDTO.setBaseName("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
        modelSourceDTO.setApiUrl("https://api.siliconflow.cn");
        modelSourceDTO.setAppKey("sk-");
        AdvSettingDTO advSettingDTO = new AdvSettingDTO();
        advSettingDTO.setParams(AIModelParamType.TEMPERATURE);
        advSettingDTO.setName("温度");
        advSettingDTO.setDefaultValue(0.7);
        advSettingDTO.setEnable(false);
        List<AdvSettingDTO> list = new ArrayList<>();
        list.add(advSettingDTO);
        modelSourceDTO.setAdvSettingDTOList(list);
       /* String response = ChatToolEngine.builder(AIModelType.DEEP_SEEK,
                        AIChatOptions.builder()
                                .modelType("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B")
                                .apiKey("sk-eaglapmczkqersgjkhodhadngxhcsokapygezyovozbzaypm")
                                .baseUrl("https://api.siliconflow.cn")
                                .build())
                .prompt("How are you?")
                .execute();
        System.out.println(response);*/
        this.requestPost(EDIT_SOURCE, modelSourceDTO).andExpect(status().is5xxServerError());

    }

    @Test
    @Order(1)
    public void testList() throws Exception {
        ModelSourceRequest request = new ModelSourceRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setOwner(DEFAULT_ORGANIZATION_ID);
       /* String response = ChatToolEngine.builder(AIModelType.DEEP_SEEK,
                        AIChatOptions.builder()
                                .modelType("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B")
                                .apiKey("sk-eaglapmczkqersgjkhodhadngxhcsokapygezyovozbzaypm")
                                .baseUrl("https://api.siliconflow.cn")
                                .build())
                .prompt("How are you?")
                .execute();
        System.out.println(response);*/
        this.requestPostWithOk(GET_LIST, request);

    }
    @Test
    @Order(2)
    public void testDetail() throws Exception {

        this.requestGet(DETAIL+"1").andExpect(status().is5xxServerError());

    }


}


