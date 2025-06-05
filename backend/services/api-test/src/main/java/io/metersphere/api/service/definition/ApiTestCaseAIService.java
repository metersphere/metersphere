package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.ApiCaseAIConfigDTO;
import io.metersphere.api.dto.definition.ApiAIResponse;
import io.metersphere.api.dto.definition.ApiGenerateInfo;
import io.metersphere.api.dto.definition.ApiTestCaseAIRequest;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.utils.ApiCasePromptTemplateCache;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiUserPromptConfig;
import io.metersphere.system.domain.AiUserPromptConfigExample;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.mapper.AiUserPromptConfigMapper;
import io.metersphere.system.service.AiChatBaseService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseAIService {

    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    AiChatBaseService aiChatBaseService;
    @Resource
    ApiCasePromptTemplateCache apiCasePromptTemplateCache;

    @Resource
    private AiUserPromptConfigMapper aiUserPromptConfigMapper;


    public String generateApiTestCase(ApiTestCaseAIRequest request, AiModelSourceDTO module) {
        ApiDefinitionBlob blob = apiDefinitionBlobMapper.selectByPrimaryKey(request.getApiDefinitionId());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class);
        String prompt = request.getPrompt() + "\n" + "以下是接口的定义的json格式数据,根据接口定义生成接口用例:\n" +
                JSON.toJSONString(BeanUtils.copyBean(new ApiAIResponse(), msTestElement));

        if (blob.getResponse() != null) {
            prompt += "\n以下是接口的定义的响应示例\n" + new String(blob.getResponse());
        }
        request.setPrompt(prompt);

        return aiChatBaseService.chatWithMemory(request, module, apiCasePromptTemplateCache.getTemplate())
                .content();
    }

    public String chat(ApiTestCaseAIRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);

        String prompt = "下面一段话中是否需要生成用例？需要生成几条用例？\n" + request.getPrompt();
        ApiGenerateInfo apiGenerateInfo = aiChatBaseService.chat(prompt, module)
                .entity(ApiGenerateInfo.class);

        if (BooleanUtils.isTrue(apiGenerateInfo.getGenerateCase())) {
            // 判断对话是否是需要生成用例
            return generateApiTestCase(request, module);
        } else {
            return aiChatBaseService.chatWithMemory(request, module).content();
        }
    }



    /**
     * 获取用户的AI提示词
     *
     * @param userId 用户ID
     */
    public ApiCaseAIConfigDTO getUserPrompt(String userId) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.API_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            return new ApiCaseAIConfigDTO();
        }
        AiUserPromptConfig aiUserPromptConfig = aiUserPromptConfigs.getFirst();
        // 解析AI模型用例生成方法提示词
        String configStr = new String(aiUserPromptConfig.getConfig(), StandardCharsets.UTF_8);
        return JSON.parseObject(configStr, ApiCaseAIConfigDTO.class);
    }

    /**
     * 保存用户的AI提示词
     *
     * @param userId 用户ID
     * @param promptDTO 包含AI模型用例生成方法提示词和生成用例的提示语
     */
    public void saveUserPrompt(String userId, ApiCaseAIConfigDTO promptDTO) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.API_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExample(example);
        AiUserPromptConfig aiUserPromptConfig;
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            aiUserPromptConfig = new AiUserPromptConfig();
            aiUserPromptConfig.setUserId(userId);
            aiUserPromptConfig.setType(AIConfigConstants.AiPromptType.API_CASE.toString());
        } else {
            aiUserPromptConfig = aiUserPromptConfigs.getFirst();
        }
        String configStr = JSON.toJSONString(promptDTO);
        aiUserPromptConfig.setConfig(configStr.getBytes(StandardCharsets.UTF_8));
        if (aiUserPromptConfig.getId() == null) {
            aiUserPromptConfig.setId(IDGenerator.nextStr());
            aiUserPromptConfigMapper.insert(aiUserPromptConfig);
        } else {
            aiUserPromptConfigMapper.updateByPrimaryKey(aiUserPromptConfig);
        }
    }
}
