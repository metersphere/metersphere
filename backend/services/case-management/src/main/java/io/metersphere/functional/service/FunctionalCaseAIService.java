package io.metersphere.functional.service;

import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.request.FunctionalCaseAIRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiUserPromptConfig;
import io.metersphere.system.domain.AiUserPromptConfigExample;
import io.metersphere.system.dto.request.ai.AIChatOption;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.mapper.AiUserPromptConfigMapper;
import io.metersphere.system.service.AiChatBaseService;
import io.metersphere.system.uid.IDGenerator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FunctionalCaseAIService {

    @Resource
    private AiUserPromptConfigMapper aiUserPromptConfigMapper;
    @Resource
    private AiChatBaseService aiChatBaseService;
    @Value("classpath:/prompts/generate.st")
    private org.springframework.core.io.Resource promptResource;

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
            // 如果没有配置，则返回一个空的配置对象
            return getFunctionalCaseAIConfigDTO();
        }
        AiUserPromptConfig aiUserPromptConfig = aiUserPromptConfigs.getFirst();
        // 解析AI模型用例生成方法提示词
        String configStr = new String(aiUserPromptConfig.getConfig(), StandardCharsets.UTF_8);
        return JSON.parseObject(configStr, FunctionalCaseAIConfigDTO.class);
    }


    /**
     * 获取默认的AI提示词配置
     */
    @NotNull
    private static FunctionalCaseAIConfigDTO getFunctionalCaseAIConfigDTO() {
        FunctionalCaseAIDesignConfigDTO aiDesignConfigDTO = new FunctionalCaseAIDesignConfigDTO();
        FunctionalCaseAITemplateConfigDTO templateConfigDTO = new FunctionalCaseAITemplateConfigDTO();
        FunctionalCaseAIConfigDTO functionalCaseAIConfigDTO = new FunctionalCaseAIConfigDTO();
        functionalCaseAIConfigDTO.setDesignConfig(aiDesignConfigDTO);
        functionalCaseAIConfigDTO.setTemplateConfig(templateConfigDTO);
        return functionalCaseAIConfigDTO;
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
            aiUserPromptConfigMapper.updateByPrimaryKeyWithBLOBs(aiUserPromptConfig);
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

    /**
     * AI对话
     *
     * @param request 请求参数
     * @param userId 用户ID
     * @return 返回对话内容
     */
    public String chat(FunctionalCaseAIRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        aiChatBaseService.saveUserConversationContent(request.getConversationId(), request.getPrompt());

        String prompt = "判断我下面这段话中是否需要生成用例，是的话返回 true，不是的话返回 false？文本如下：\n" + request.getPrompt();
        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(module)
                .system("你只负责判断是否包含“生成测试用例”的请求意图，不能生成用例本身。\n")
                .prompt(prompt)
                .build();
        Boolean isGenerateCase = aiChatBaseService.chat(aiChatOption)
                .entity(Boolean.class);

        if (Boolean.TRUE.equals(isGenerateCase)) {
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptResource);
            Message systemMessage = systemPromptTemplate.createMessage();
            aiChatOption.setSystem(systemMessage.toString());
            aiChatOption.setPrompt(buildUserPromptTpl(userId, request.getPrompt()));
        } else {
            aiChatOption.setSystem(null);
            aiChatOption.setPrompt(request.getPrompt());
        }
        String content = aiChatBaseService.chatWithMemory(aiChatOption).content();
        aiChatBaseService.saveAssistantConversationContent(request.getConversationId(), content);
        return content;
    }

    /**
     * 构建用户提示词模板
     * @param userId 用户ID
     * @param input 用户输入
     * @return 提示词
     */
    private String buildUserPromptTpl(String userId, String input) {
        FunctionalCaseAIConfigDTO userPrompt = getUserPrompt(userId);
        FunctionalCaseAITemplateConfigDTO templateConfig = userPrompt.getTemplateConfig();
        List<String> modules = new ArrayList<>();
        if (templateConfig != null) {
            for (Field field : FunctionalCaseAITemplateConfigDTO.class.getFields()) {
                Schema annotation = field.getAnnotation(Schema.class);
                try {
                    Object value = field.get(templateConfig);
                    if (value instanceof String) {
                        modules.add(StringUtils.equals("STEP", value.toString()) ? "步骤描述" : "文本描述");
                    }
                    if (value instanceof Boolean && Boolean.TRUE.equals(value)) {
                        modules.add(annotation.description());
                    }
                } catch (IllegalAccessException e) {
                    LogUtils.error(e.getMessage());
                }
            }
        } else {
            modules.addAll(Arrays.asList("用例名称", "前置条件", "文本描述", "备注"));
        }


        FunctionalCaseAIDesignConfigDTO designConfig = userPrompt.getDesignConfig();
        List<String> designs = new ArrayList<>();
        if (designConfig != null) {
            for (Field field : FunctionalCaseAIDesignConfigDTO.class.getFields()) {
                if (field.isAnnotationPresent(Schema.class) && !StringUtils.equalsAny(field.getName(), "normal", "abnormal")) {
                    Schema annotation = field.getAnnotation(Schema.class);
                    try {
                        Object value = field.get(designConfig);
                        if (Boolean.TRUE.equals(value)) {
                            designs.add(annotation.description());
                        }
                    } catch (IllegalAccessException e) {
                        LogUtils.error(e.getMessage());
                    }
                }
            }
        } else {
            designs.addAll(Arrays.asList("等价类划分", "边界值分析", "决策表测试", "因果图法", "正交实验法", "场景法", "场景法描述"));
        }
        return String.format("%s，合理运用测试方法: %s，只需包含以下模块: %s",
                input, String.join(", ", designs), String.join(", ", modules));
    }
}
