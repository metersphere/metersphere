package io.metersphere.functional.service;

import io.metersphere.functional.dto.FunctionalCaseAIConfigDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiUserPromptConfig;
import io.metersphere.system.domain.AiUserPromptConfigExample;
import io.metersphere.system.mapper.AiUserPromptConfigMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class FunctionalCaseAIService {

    @Resource
    private AiUserPromptConfigMapper aiUserPromptConfigMapper;

    /**
     * 获取用户的AI提示词
     *
     * @param userId 用户ID
     * @return FunctionalCaseAIPromptDTO 包含AI模型用例生成方法提示词和生成用例的提示语
     */
    public FunctionalCaseAIConfigDTO getUserPrompt(String userId) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.FUNCTIONAL_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            return new FunctionalCaseAIConfigDTO();
        }
        AiUserPromptConfig aiUserPromptConfig = aiUserPromptConfigs.getFirst();
        // 解析AI模型用例生成方法提示词
        String configStr = new String(aiUserPromptConfig.getConfig(), StandardCharsets.UTF_8);
        return JSON.parseObject(configStr, FunctionalCaseAIConfigDTO.class);
    }

    /**
     * 保存用户的AI提示词
     *
     * @param userId 用户ID
     * @param promptDTO 包含AI模型用例生成方法提示词和生成用例的提示语
     */
    public void saveUserPrompt(String userId, FunctionalCaseAIConfigDTO promptDTO) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.FUNCTIONAL_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExample(example);
        AiUserPromptConfig aiUserPromptConfig;
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            aiUserPromptConfig = new AiUserPromptConfig();
            aiUserPromptConfig.setUserId(userId);
            aiUserPromptConfig.setType(AIConfigConstants.AiPromptType.FUNCTIONAL_CASE.toString());
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
