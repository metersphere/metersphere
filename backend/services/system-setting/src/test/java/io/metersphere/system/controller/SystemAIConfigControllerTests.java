package io.metersphere.system.controller;

import io.metersphere.ai.engine.common.AIModelParamType;
import io.metersphere.ai.engine.common.AIModelType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiModelSource;
import io.metersphere.system.dto.request.ai.AdvSettingDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceRequest;
import io.metersphere.system.mapper.AiModelSourceMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemAIConfigControllerTests  extends BaseTest {

    public static final String BASE_PATH = "/ai/config";
    public static final String EDIT_SOURCE = "/edit-source";
    public static final String GET_LIST = "/source/list";
    public static final String DETAIL = "/get/";
    public static final String GET_NAME_LIST = "/source/name/list";
    public static final String DELETE = "/delete/";



    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }


    @Resource
    private AiModelSourceMapper aiModelSourceMapper;


    private String saveModel(String name){
        String id = IDGenerator.nextStr();
        AiModelSource aiModelSource = new AiModelSource();
        aiModelSource.setId(id);
        aiModelSource.setType("LLM");
        aiModelSource.setName(name);
        aiModelSource.setProviderName(AIModelType.DEEP_SEEK);
        aiModelSource.setPermissionType(AIConfigConstants.AiPermissionType.PUBLIC.toString());
        aiModelSource.setOwnerType(AIConfigConstants.AiOwnerType.SYSTEM.toString());
        aiModelSource.setBaseName("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
        aiModelSource.setApiUrl("https://api.siliconflow.cn");
        aiModelSource.setAppKey("sk-rtgghhjkkll");
        aiModelSource.setStatus(false);
        aiModelSource.setOwner(DEFAULT_ORGANIZATION_ID);
        aiModelSource.setCreateTime(System.currentTimeMillis());
        aiModelSource.setCreateUser("admin");
        AdvSettingDTO advSettingDTO = new AdvSettingDTO();
        advSettingDTO.setName(AIModelParamType.TEMPERATURE);
        advSettingDTO.setLabel("温度");
        advSettingDTO.setValue(0.7);
        advSettingDTO.setEnable(false);
        AdvSettingDTO advSettingDTO1 = new AdvSettingDTO();
        advSettingDTO1.setName(AIModelParamType.MAX_TOKENS);
        advSettingDTO1.setLabel("最大Token数");
        advSettingDTO1.setValue(null);
        advSettingDTO1.setEnable(null);
        AdvSettingDTO advSettingDTO2 = new AdvSettingDTO();
        advSettingDTO2.setName(AIModelParamType.FREQUENCY_PENALTY);
        advSettingDTO2.setLabel("频率惩罚");
        advSettingDTO2.setValue(null);
        advSettingDTO2.setEnable(null);
        AdvSettingDTO advSettingDTO3 = new AdvSettingDTO();
        advSettingDTO3.setName(AIModelParamType.TOP_P);
        advSettingDTO3.setLabel("Top P");
        advSettingDTO3.setValue(null);
        advSettingDTO3.setEnable(null);
        List<AdvSettingDTO> list = new ArrayList<>();
        list.add(advSettingDTO);
        list.add(advSettingDTO1);
        list.add(advSettingDTO2);
        list.add(advSettingDTO3);
        aiModelSource.setAdvSettings(JSON.toJSONString(advSettingDTO));
        aiModelSourceMapper.insert(aiModelSource);
        return id;
    }


    @Test
    @Order(0)
    public void testEdit() throws Exception {
        String s = saveModel("测试模型1");
        AiModelSourceDTO aiModelSourceDTO = new AiModelSourceDTO();
        aiModelSourceDTO.setName("测试模型");
        aiModelSourceDTO.setType("LLM");
        aiModelSourceDTO.setId(s);
        aiModelSourceDTO.setProviderName(AIModelType.DEEP_SEEK);
        aiModelSourceDTO.setPermissionType(AIConfigConstants.AiPermissionType.PUBLIC.toString());
        aiModelSourceDTO.setOwnerType(AIConfigConstants.AiOwnerType.SYSTEM.toString());
        aiModelSourceDTO.setBaseName("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
        aiModelSourceDTO.setApiUrl("https://api.siliconflow.cn");
        aiModelSourceDTO.setAppKey("sk-r**** ll");
        AdvSettingDTO advSettingDTO = new AdvSettingDTO();
        advSettingDTO.setName(AIModelParamType.TEMPERATURE);
        advSettingDTO.setLabel("温度");
        advSettingDTO.setValue(0.7);
        advSettingDTO.setEnable(false);
        List<AdvSettingDTO> list = new ArrayList<>();
        list.add(advSettingDTO);
        aiModelSourceDTO.setAdvSettingDTOList(list);
        aiModelSourceDTO.setName("测试模型1");
        aiModelSourceDTO.setCreateUser("admin");
        this.requestPost(EDIT_SOURCE, aiModelSourceDTO).andExpect(status().isOk());
        aiModelSourceDTO.setStatus(true);
        aiModelSourceDTO.setAppKey("sk-rfghll");
        this.requestPost(EDIT_SOURCE, aiModelSourceDTO).andExpect(status().is5xxServerError());
        aiModelSourceDTO.setId(null);
        aiModelSourceDTO.setStatus(true);
        this.requestPost(EDIT_SOURCE, aiModelSourceDTO).andExpect(status().is5xxServerError());
        aiModelSourceDTO = new AiModelSourceDTO();
        aiModelSourceDTO.setName("智谱 THUDM/GLM-Z1-9B-0414");
        aiModelSourceDTO.setType("LLM");
        aiModelSourceDTO.setProviderName(AIModelType.ZHI_PU_AI);
        aiModelSourceDTO.setPermissionType(AIConfigConstants.AiPermissionType.PUBLIC.toString());
        aiModelSourceDTO.setOwnerType(AIConfigConstants.AiOwnerType.SYSTEM.toString());
        aiModelSourceDTO.setBaseName("THUDM/GLM-Z1-9B-0414");
        aiModelSourceDTO.setApiUrl("https://api.siliconflow.cn");
        aiModelSourceDTO.setAppKey("sk-eaglapmcezyovozbzaypm");
        aiModelSourceDTO.setAdvSettingDTOList(new ArrayList<>());
        this.requestPost(EDIT_SOURCE, aiModelSourceDTO).andExpect(status().isOk());
        aiModelSourceDTO.setAppKey("rrr");
        aiModelSourceDTO.setName("智谱");
        aiModelSourceDTO.setId(s);
        aiModelSourceDTO.setCreateUser("admin");
        this.requestPost(EDIT_SOURCE, aiModelSourceDTO).andExpect(status().isOk());
        this.requestGetWithOk(DETAIL+s);

    }

    @Test
    @Order(1)
    public void testList() throws Exception {
        saveModel("测试模型1");
        AiModelSourceRequest request = new AiModelSourceRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setOwner(DEFAULT_ORGANIZATION_ID);
        this.requestPostWithOk(GET_LIST, request);

    }
    @Test
    @Order(2)
    public void testDetail() throws Exception {
        this.requestGet(DETAIL+"1").andExpect(status().is5xxServerError());
        String id = saveModel("测试模型2");
        this.requestGetWithOk(DETAIL+id);
    }
    @Test
    @Order(3)
    public void testNameList() throws Exception {
        String id = saveModel("测试模型3");
        this.requestGetWithOk(GET_NAME_LIST);
    }

    @Test
    @Order(4)
    public void testDelete() throws Exception {
        String id = saveModel("测试模型4");
        this.requestGetWithOk(DELETE+id);
    }

}


