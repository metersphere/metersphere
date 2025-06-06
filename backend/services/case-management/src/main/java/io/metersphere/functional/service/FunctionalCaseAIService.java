package io.metersphere.functional.service;

import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.dto.*;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiUserPromptConfig;
import io.metersphere.system.domain.AiUserPromptConfigExample;
import io.metersphere.system.dto.request.ai.AIChatOption;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.mapper.AiUserPromptConfigMapper;
import io.metersphere.system.service.AiChatBaseService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class FunctionalCaseAIService {

    @Resource
    private AiUserPromptConfigMapper aiUserPromptConfigMapper;

    @Resource
    AiChatBaseService aiChatBaseService;

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

    public FunctionalCaseAiDTO transformToDTO(AIChatRequest request, String userId) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.FUNCTIONAL_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExampleWithBLOBs(example);
        FunctionalCaseAiDTO functionalCaseAiDTO = new FunctionalCaseAiDTO();
        //如果用户没配置，默认返回文本类型步骤描述
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            functionalCaseAiDTO.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.TEXT.name());
        }else {
            AiUserPromptConfig first = aiUserPromptConfigs.getFirst();
            String configStr = new String(first.getConfig(), StandardCharsets.UTF_8);
            FunctionalCaseAIConfigDTO configDTO = JSON.parseObject(configStr, FunctionalCaseAIConfigDTO.class);
            FunctionalCaseAITemplateConfigDTO templateConfig = configDTO.getTemplateConfig();
            functionalCaseAiDTO.setCaseEditType(templateConfig.getCaseEditType() != null ? templateConfig.getCaseEditType() : FunctionalCaseTypeConstants.CaseEditType.TEXT.name());
        }
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        String prompt = "请解析以下格式并转为java对象:\n" + request.getPrompt();
        AIChatOption aiChatOption = AIChatOption.builder()
                .module(module)
                .prompt(prompt)
                .build();
        //根据用户配置的编辑类型，返回不同的实体类
        if (StringUtils.equalsIgnoreCase(functionalCaseAiDTO.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.STEP.name())) {
            FunctionalCaseStepAiDTO entity = aiChatBaseService.chat(aiChatOption).entity(FunctionalCaseStepAiDTO.class);
            BeanUtils.copyBean(functionalCaseAiDTO,entity);
            functionalCaseAiDTO.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.STEP.name());
        } else {
            FunctionalCaseTextAiDTO entity = aiChatBaseService.chat(aiChatOption).entity(FunctionalCaseTextAiDTO.class);
            BeanUtils.copyBean(functionalCaseAiDTO,entity);
            functionalCaseAiDTO.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.TEXT.name());
        }
        return functionalCaseAiDTO;
    }
}
